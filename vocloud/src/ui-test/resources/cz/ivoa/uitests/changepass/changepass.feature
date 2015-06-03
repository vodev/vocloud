@ChangePass
Feature: Page that allows logged users to change their password

Scenario: Checking submitting with no input
    Given I am logged in as testGroupUser
    Given I am on change password page
    When I click Submit button
    Then Old password error details show Old password: Validation Error: Value is required.
    And New password error details show New password: Validation Error: Value is required.
    And New password again error details show New password again: Validation Error: Value is required.

Scenario: Old password is invalid
    Given I am logged in as testGroupUser
    Given I am on change password page
    When I type old password wrongPassword321
    And I type new password 1 newPass123
    And I type new password 2 newPass123
    And I click Submit button
    Then Old password error details show Wrong password.

Scenario: New password not matching
    Given I am logged in as testGroupUser
    Given I am on change password page
    When I type old password testGroupUser123
    And I type new password 1 newPass123
    And I type new password 2 newPass666
    And I click Submit button
    Then Global warning message is shown Password does not match

Scenario: Standard scenario login -> change password -> logout -> login with new password
    Given I am logged in as testGroupUser
    Given I am on change password page
    When I type old password testGroupUser123
    And I type new password 1 newPass123
    And I type new password 2 newPass123
    And I click Submit button
    Then Global info message is shown Your password has been successfully changed.
    When I click Logout
    And I type username testGroupUser
    And I type password newPass123
    And I click Login button
    Then I am logged in
    