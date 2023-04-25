package com.innovattic.medicinfo.logic.dto.triage

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.innovattic.medicinfo.logic.triage.model.Descriptive
import com.innovattic.medicinfo.logic.triage.model.MultipleChoice
import com.innovattic.medicinfo.logic.triage.model.SingleChoice
import com.innovattic.medicinfo.logic.triage.model.StoredAnswer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class StoredAnswerDeserializationTest {

    @Test
    fun `given Int as answer value, expect instance of SingleChoice`() {
        val json = """
        {"questionId": "ABC1", "answer": 3, "type": "single_selection"}        
        """

        val answer = StoredAnswer.ofJson(json)

        assertTrue(answer is SingleChoice)
        val castAnswer = answer as SingleChoice
        assertEquals("ABC1", castAnswer.questionId)
        assertEquals(3, castAnswer.answer)
    }

    @Test
    fun `given array as answer value, expect instance of MultipleChoice`() {
        val json = """
        {"questionId": "ABC1", "answer": [1,2,3], "type": "multi_selection"}        
        """

        val answer = StoredAnswer.ofJson(json)

        assertTrue(answer is MultipleChoice)
        val castAnswer = answer as MultipleChoice
        assertEquals("ABC1", castAnswer.questionId)
        assertTrue(listOf(1, 2, 3).containsAll(castAnswer.answer))
    }

    @Test
    fun `given string as selectedAnswer value, expect instance of Descriptive`() {
        val json = """
        {"questionId": "ABC1", "answer": "descriptive value", "type": "descriptive"}        
        """

        val answer = StoredAnswer.ofJson(json)

        assertTrue(answer is Descriptive)
        val castAnswer = answer as Descriptive
        assertEquals("ABC1", castAnswer.questionId)
        assertEquals("descriptive value", castAnswer.answer)
    }

    @Test
    fun `given null as answer value for single-choice type, expect MismatchedInputException`() {
        val json = """
        {"questionId": "ABC1", "answer": null, "type": "single_selection"}        
        """

        assertThrows(MismatchedInputException::class.java) {
            StoredAnswer.ofJson(json)
        }
    }

    @Test
    fun `given null as answer value for multiple-choice type, expect MissingKotlinParameterException`() {
        val json = """
        {"questionId": "ABC1", "answer": null, "type": "multi_selection"}        
        """

        assertThrows(MissingKotlinParameterException::class.java) {
            StoredAnswer.ofJson(json)
        }
    }

    @Test
    fun `given null as questionId value, expect MissingKotlinParameterException`() {
        val json = """
        {"questionId": null , "answer": 1, "type": "single_selection"}        
        """

        assertThrows(MissingKotlinParameterException::class.java) {
            StoredAnswer.ofJson(json)
        }
    }
}
