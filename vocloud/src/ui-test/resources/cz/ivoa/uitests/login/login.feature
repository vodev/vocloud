@Login
Feature: Logging page of the vocloud system

Scenario: Logging with invalid credentials
    Given I am on login page
    When I type username wrongUsername
    And I type password someWrongPass123
    And I click login button
    Then Login error page is shown

Scenario: Logging as common user
    Given I am on login page
    When I type username testGroupUser
    And I type password testGroupUser123
    And I click login button
    Then I am logged in
    And View filesystem button is visible

Scenario: Logging as manager user
    Given I am on login page
    When I type username testGroupManager
    And I type password testGroupManager123
    And I click login button
    Then I am logged in
    And Manage filesystem button is visible

Scenario: Logging as admin user
    Given I am on login page
    When I type username testGroupAdmin
    And I type password testGroupAdmin123
    And I click login button
    Then I am logged in
    And Manage filesystem button is visible
