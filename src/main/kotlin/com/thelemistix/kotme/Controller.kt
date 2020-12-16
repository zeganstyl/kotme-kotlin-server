package com.thelemistix.kotme

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class Controller {
    @Autowired
    lateinit var scriptService: ScriptService

    @GetMapping("/")
    fun index(): String = scriptService.checkRunning()

    @PostMapping("/",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun checkExercise(@RequestParam param: Map<String, String>): String {
        return scriptService.checkExercise(param)
    }
}
