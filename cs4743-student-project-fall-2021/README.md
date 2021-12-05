**Running SpringBoot / Hibernate**
These should run automatically if running AppMain.

**Notes**
- If using Postman, be sure to send the _SHA-256_ encrypted password. If using the application within this project, it will do that automatically.
- Session tokens will change every login. Essencially, if one logs into Postman, then into this application, Postman will be "kicked" out of the session.
- Deleting and updating may be difficult to test with Postman, as id's have been removed or added countless times due to testing. In the application, knowing the id's are not needed. If using Postman, some valid id's are: 1, 2, 21, 22, and 24.

**Login Credentials**

Same as previous assignments.

_Note: if testing with Postman, the passwords have to be encrypted with SHA-256._


**Template for Updating and Adding Person in Postman**

"firstName":"name",
"lastName":"name",
"dateOfBirth":"date"


