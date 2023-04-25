package com.innovattic.medicinfo.web.endpoint

object Swagger {

    const val PERMISSION_PUBLIC = "Available to any caller, no authentication required"
    const val PERMISSION_ADMIN = "Permissions: Admin only"
    const val PERMISSION_EMPLOYEE = "Permissions: Admin & employee (all object(s))"
    const val PERMISSION_ALL = "$PERMISSION_EMPLOYEE, customer (your own object(s))"
    const val PERMISSION_CUSTOMER = "Permission: customer only (your own object(s))"

    const val FIQL = "A [FIQL query](https://datatracker.ietf.org/doc/html/draft-nottingham-atompub-fiql-00). "
    const val FIQL_ALL = FIQL + "All fields from the returned DTO are supported."
    const val FIQL_EXAMPLE = "created=gt=2021-01-01T12:00Z;displayName=contains=user"

    const val USER_DTO = "\n\nReturns any of the 3 user dto types. " +
            "You can separate them out using the role field, or just use CustomerDto which supports all fields."
    const val REGISTER_CUSTOMER = "This endpoint requires custom authorization, see Innovattic internal documentation for details.\n\n" +
            "Allows a customer to register their own account."

    const val CONFIGURE_SELF_TEST = PERMISSION_ADMIN +
            "\n\nThis endpoint validates properties in addition to those mentioned in the DTOs themselves:\n" +
            "* The question codes must be unique across all questions\n" +
            "* "
}
