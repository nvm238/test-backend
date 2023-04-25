package com.innovattic.medicinfo.web.endpoint

import com.innovattic.common.test.assertErrorResponse
import com.innovattic.common.test.assertListResponse
import com.innovattic.medicinfo.web.BaseEndpointTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GeneralPracticeEndpointsTest : BaseEndpointTest() {

    @Test
    fun `Get general practices works for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
            queryParam("contracted", false)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertListResponse(2)
            body("[0].code", equalTo("0"))
            body("[0].name", equalTo("general practice without contract"))
            body("[1].code", equalTo("1"))
            body("[1].name", equalTo("general practice with contract"))
        }
    }

    @Test
    fun `Get general practices returns everything without contracted parameter for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertListResponse(2)
            body("[0].code", equalTo("0"))
            body("[0].name", equalTo("general practice without contract"))
            body("[1].code", equalTo("1"))
            body("[1].name", equalTo("general practice with contract"))
        }
    }

    @Test
    fun `Get general practices fails with unknown label`() {
        Given {
            queryParam("labelCode", "AAA")
            queryParam("contracted", false)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices works with no label`() {
        Given {
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertListResponse(1)
            body("[0].code", equalTo("1"))
            body("[0].name", equalTo("general practice with contract"))
        }
    }

    @Test
    fun `Get general practices with contract works for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertListResponse(1)
            body("[0].code", equalTo("1"))
            body("[0].name", equalTo("general practice with contract"))
        }
    }

    @Test
    fun `Get general practices with contract fails with unknown label`() {
        Given {
            queryParam("labelCode", "AAA")
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices with contract works with no label`() {
        Given {
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/practice")
        } Then {
            assertListResponse(1)
            body("[0].code", equalTo("1"))
            body("[0].name", equalTo("general practice with contract"))
        }
    }

    @Test
    fun `Get general practice centers works for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
            queryParam("contracted", false)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertListResponse(2)
            body("[0].code", equalTo("0"))
            body("[0].name", equalTo("general practice center without contract"))
            body("[1].code", equalTo("1"))
            body("[1].name", equalTo("general practice center with contract"))
        }
    }

    @Test
    fun `Get general practice centers returns everything without contracted parameter for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertListResponse(2)
            body("[0].code", equalTo("0"))
            body("[0].name", equalTo("general practice center without contract"))
            body("[1].code", equalTo("1"))
            body("[1].name", equalTo("general practice center with contract"))
        }
    }

    @Test
    fun `Get general practices centers fails with unknown label`() {
        Given {
            queryParam("labelCode", "AAA")
            queryParam("contracted", false)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices centers fails with no label`() {
        Given {
            queryParam("contracted", false)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Get general practice centers with contract works for label`() {
        val label = createLabel()

        Given {
            queryParam("labelCode", label.code)
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertListResponse(1)
            body("[0].code", equalTo("1"))
            body("[0].name", equalTo("general practice center with contract"))
        }
    }


    @Test
    fun `Get general practices centers with contract fails with unknown label`() {
        Given {
            queryParam("labelCode", "AAA")
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/center/")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices centers with contract fails with no label`() {
        Given {
            queryParam("contracted", true)
        } When {
            get("v1/general-practice/center")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `Get general practice practitioners works for label`() {
        val label = createLabel()
        val generalPracticeCode = 0

        Given {
            queryParam("labelCode", label.code)
        } When {
            get("v1/general-practice/practice/$generalPracticeCode/practitioner")
        } Then {
            assertListResponse(1)
            body("[0].code", equalTo("0"))
            body("[0].name", equalTo("general practice practitioner"))
        }
    }


    @Test
    fun `Get general practices practitioners fails with unknown label`() {
        val generalPracticeCode = 0

        Given {
            queryParam("labelCode", "AAA")
        } When {
            get("v1/general-practice/practice/$generalPracticeCode/practitioner")
        } Then {
            assertErrorResponse(null, HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `Get general practices practitioners fails with no label`() {
        val generalPracticeCode = 0

        When {
            get("v1/general-practice/practice/$generalPracticeCode/practitioner")
        } Then {
            assertErrorResponse(null, HttpStatus.BAD_REQUEST)
        }
    }
}
