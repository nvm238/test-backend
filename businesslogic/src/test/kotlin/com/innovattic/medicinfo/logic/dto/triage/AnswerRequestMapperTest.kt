package com.innovattic.medicinfo.logic.dto.triage

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AnswerRequestMapperTest {

    @Test
    fun `given Int as selectedAnswer value, expect instance of SingleChoiceAnswer`() {
        val json = """
        {"questionId": "ABC1", "answer": 3, "type": "single_selection"}        
        """

        val answer = AnswerRequest.ofJson(json)

        assertTrue(answer is SingleChoiceAnswer)
        val castAnswer = answer as SingleChoiceAnswer
        assertEquals("ABC1", castAnswer.questionId)
        assertEquals(3, castAnswer.answer)
    }

    @Test
    fun `given Int as selectedAnswer value, expect instance of Slider`() {
        val json = """
        {"questionId": "ABC1", "answer": 3, "type": "slider"}        
        """

        val answer = AnswerRequest.ofJson(json)

        assertTrue(answer is SliderAnswer)
        val castAnswer = answer as SliderAnswer
        assertEquals("ABC1", castAnswer.questionId)
        assertEquals(3, castAnswer.answer)
        println(answer.toJson())
    }

    @Test
    fun `given array as selectedAnswer value, expect instance of MultipleChoiceAnswer`() {
        val json = """
        {"questionId": "ABC1", "answer": [1,2,3], "type": "multi_selection"}        
        """

        val answer = AnswerRequest.ofJson(json)

        assertTrue(answer is MultipleChoiceAnswer)
        val castAnswer = answer as MultipleChoiceAnswer
        assertEquals("ABC1", castAnswer.questionId)
        assertTrue(listOf(1, 2, 3).containsAll(castAnswer.answer))
    }

    @Test
    fun `given string as selectedAnswer value, expect instance of StringAnswer`() {
        val json = """
        {"questionId": "ABC1", "answer": "descriptive value", "type": "descriptive"}        
        """

        val answer = AnswerRequest.ofJson(json)

        assertTrue(answer is StringAnswer)
        val castAnswer = answer as StringAnswer
        assertEquals("ABC1", castAnswer.questionId)
        assertEquals("descriptive value", castAnswer.answer)
    }

    @Test
    fun `given null as selectedAnswer value, expect MismatchedInputException`() {
        val json = """
        {"questionId": "ABC1", "answer": null, "type": "single_selection"}        
        """

        assertThrows(MismatchedInputException::class.java) {
            AnswerRequest.ofJson(json)
        }
    }

    @Test
    fun `given null as questionId value, expect MissingKotlinParameterException`() {
        val json = """
        {"questionId": null , "answer": 1, "type": "single_selection"}        
        """

        assertThrows(MissingKotlinParameterException::class.java) {
            AnswerRequest.ofJson(json)
        }
    }
}
