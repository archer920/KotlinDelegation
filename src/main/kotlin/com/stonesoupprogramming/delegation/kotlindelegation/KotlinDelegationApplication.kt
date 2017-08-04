package com.stonesoupprogramming.delegation.kotlindelegation

import org.hibernate.validator.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotNull

@SpringBootApplication
class KotlinDelegationApplication

enum class FamilyMemberType {Father, Mother, Daughter, Son}

//Basic entity class
@Entity
data class Belchers(
        @field: Id
        @field: GeneratedValue
        var id : Long? = null,

        @field: NotBlank(message = "Need a name!")
        var name : String = "",

        @field: NotNull(message = "Assign to a family type")
        var familyMemberType: FamilyMemberType? = null
)

//Now we are going to define a JpaRepository to handle persistence
interface BelchersRepository : JpaRepository<Belchers, Long>

//Here is a service class that contains our business logic
@Service
@Transactional
class BelchersService(
        //Inject an instance of BelchersRepository
        @field : Autowired
        val belchersRepository: BelchersRepository) : BelchersRepository by belchersRepository {
    /**
     * The above line demonstrates Kotlin's delegation syntax. It works by specifying a variable whose type
     * is an interface (no concrete or abstract classes). After the colon, we specify the name of the interface
     * and the variable that provides the object we are using for delegation. The Kotlin compiler builds out all of
     * methods included in the interface and routes calls to those method to the delegate object.
     *
     * In this example, BelcherService gets all of the methods included in BelchersRepository and the belcherRepository
     * object handles the implementation of all BelcherRepository method unless we override them.
     */

    /**
     * Here is an example of where we override only one method of BelchersRepository
     *  so that we can customize the behavior.
     */
    override fun <S : Belchers?> save(entity: S): S {
        val formattedName = entity?.name?.split(" ")?.map { it.toLowerCase().capitalize() }?.joinToString(" ")
        if(formattedName != null){
            entity.name = formattedName
        }
        return belchersRepository.save(entity)
    }
}

//Example MVC controller
@Controller
@RequestMapping("/")
class IndexController (
        @field: Autowired
        val belchersService: BelchersService) {

    @ModelAttribute("belcherFamily")
    fun fetchFamily() = belchersService.findAll()

    @ModelAttribute("belcher")
    fun fetchBelcher() = Belchers()

    @GetMapping
    fun doGet() = "index"

    @PostMapping
    fun doPost(@Valid belcher : Belchers, bindingResult: BindingResult, model: Model) : String {
        var entity = belcher

        if(!bindingResult.hasErrors()){
            belchersService.save(belcher)
            entity = Belchers()
        }

        //Notice the use of extension functions to keep the code concise
        model.addBelcher(entity)
        model.addBelcherFamily()

        return "index"
    }

    //Some private extension functions which tend to be really useful in Spring MVC
    private fun Model.addBelcherFamily(){
        addAttribute("belcherFamily", belchersService.findAll())
    }

    private fun Model.addBelcher(belcher: Belchers = Belchers()){
        addAttribute("belcher", belcher)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(KotlinDelegationApplication::class.java, *args)
}
