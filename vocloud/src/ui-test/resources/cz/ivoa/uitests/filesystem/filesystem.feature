@Filesystem
Feature: Pages view and manage filesystem 
Pages view and manage filesystem serves as web user interface for manipulating 
with the remote storage of the VO-CLOUD distributed system.

Scenario: Clicking delete button when no file or folder is selected
    Given I am logged in as manager
    Given I am on Manage filesystem page
    When I click Delete selected button
    And I click Yes on confirmation dialog
    Then Filesystem warning message is shown You must select files first

Scenario: Creating folder with empty name
    Given I am logged in as manager
    Given I am on Manage filesystem page
    When I click New folder button
    And I click Create folder button
    Then Filesystem error message is shown Name of the new folder is invalid

Scenario: Creating folder where illegal character is in the folder name
    Given I am logged in as manager
    Given I am on Manage filesystem page
    When I click New folder button
    And I type folder name test/folder
    And I click Create folder button
    Then Filesystem error message is shown Name of the new folder is invalid

Scenario: Creation, recreation with same name and deletion of folder
    Given I am logged in as manager
    Given I am on Manage filesystem page
    When I click New folder button
    And I type folder name folderForTestingPurposes
    And I click Create folder button
    Then Filesystem info message is shown Folder folderForTestingPurposes was successfully created
    And Folder folderForTestingPurposes is listed
    When I click New folder button
    And I type folder name folderForTestingPurposes
    And I click Create folder button
    Then Filesystem warning message is shown Folder with name folderForTestingPurposes already exists
    When I check folder row folderForTestingPurposes
    And I click Delete selected button
    And I click Yes on confirmation dialog
    Then Filesystem info message is shown Deletion was successful
    And Folder folderForTestingPurposes is not listed