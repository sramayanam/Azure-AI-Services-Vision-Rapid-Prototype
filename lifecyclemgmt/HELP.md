# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.1/maven-plugin/build-image.html)
* [Azure Storage](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#resource-handling)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.1/reference/web/servlet.html)
* [Azure Active Directory](https://microsoft.github.io/spring-cloud-azure/current/reference/html/index.html#spring-security-with-azure-active-directory)
* [Spring Cloud Azure developer guide](https://aka.ms/spring/msdocs/developer-guide)

### Guides
The following guides illustrate how to use some features concretely:

* [How to use the Spring Boot starter for Azure Storage](https://aka.ms/spring/msdocs/storage)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Java Web App with the Spring Boot Starter for Azure Active Directory](https://aka.ms/spring/msdocs/aad)
* [Deploying a Spring Boot app to Azure](https://spring.io/guides/gs/spring-boot-for-azure/)

### Additional Links
These additional references should also help you:

* [Azure Storage Sample](https://aka.ms/spring/samples/latest/storage)
* [Azure Active Directory Sample](https://aka.ms/spring/samples/latest/aad)
* [Azure Samples](https://aka.ms/spring/samples)

### Deploy to Azure

This project can be deployed to Azure with Maven.

To get started, replace the following placeholder in your `pom.xml` with your specific Azure details:

- `subscriptionId`
- `resourceGroup`
- `appEnvironmentName`
- `region`

Now you can deploy your application:
```bash
./mvnw azure-container-apps:deploy
```

Learn more about [Java on Azure Container Apps](https://learn.microsoft.com/azure/container-apps/java-overview).
### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

