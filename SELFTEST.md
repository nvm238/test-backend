# Self Test

## Points for Severities and problem area's
Each problem area contains questions. Each question contains answers, and a certain amount of points.
Each problem area also contains a list of minimum average amount of points needed to result in a Severity.
For example, you could have three severities:
```
severities = 
listOf(
    ConfigureSelfTestSeverity(
        0.0,
        SelfTestSeverity.NOT_IMPORTANT
    ),
    ConfigureSelfTestSeverity(
        1.5,
        SelfTestSeverity.LIGHT
    ),
    ConfigureSelfTestSeverity(
        2.5,
        SelfTestSeverity.SERIOUS
    )
)
```

If you have three questions where each question represents a certain amount of points:
Question 1, Boolean 
Yes = 3 point
No = 0 points

Question 2, Boolean
Yes = 4 point
No = 0 points

Question 3, Boolean
Yes = 1 point
No = 0 points

The advice would be calculated by a sum of all points, divided by the amount of questions.
The following scheme shows some results based on answers:
```
Question 1 | Question 2 | Question 3 | Total Points | Average Points | Severity 
YES        | YES        | YES        | 8            | 2,7            | SERIOUS
YES        | YES        | NO         | 7            | 2,3            | LIGHT
YES        | NO         | NO         | 3            | 1              | NOT_IMPORTANT
YES        | NO         | YES        | 4            | 1,3            | NOT_IMPORTANT
NO         | YES        | YES        | 5            | 1,7            | LIGHT
```

## Points for outcomes per problem area
You can configure general questions which also represent points.
As in the previous block, you will also calculate an average of the points and questions.
Each problem area has certain outcomes, for example:
```
outcomes = listOf(
    ConfigureSelfTestResultDto(
        1.0,
        "light-advice-text",
        "light-advice-chat-text"
    ),
    ConfigureSelfTestResultDto(
        2.0,
        "mild-advice-text",
        "mild-advice-chat-text"
    ),
    ConfigureSelfTestResultDto(
        3.0,
        "serious-advice-text-personal-assistance",
        "serious-advice-chat-text-personal-assistance",
        personalAssistance = true
    ),
    ConfigureSelfTestResultDto(
        3.0,
        "serious-advice-text-no-personal-assistance",
        "serious-advice-chat-text-no-personal-assistance",
        mirro = ConfigureSelfTestMirroDto(
            "title-self-test-mirro",
            "short-desc",
            "long-desc",
            "www.google.nl"
        ),
        personalAssistance = false
    ),
)
```
The outcome is based on : Points & Personal assistance  
If you have a slider question with a certain amount of points between 1-3 you could have the following results:
```
Question 1 | Total Points | Average Points | Personal Assistance | Advice text 
1          | 1            | 1              | No                  | light-advice-text
1          | 1            | 1              | Yes                 | light-advice-text
2          | 2            | 2              | No                  | mild-advice-text
2          | 2            | 2              | Yes                 | mild-advice-text
NOTE: As you can see, above it does not matter wether or not personal assistance is true/false, since it was not configured
3          | 3            | 3              | No                  | serious-advice-text-no-personal-assistance
3          | 3            | 3              | Yes                 | serious-advice-text-personal-assistance
NOTE: As you can see, above the personal assistance does matter, since it was configured that way.
```