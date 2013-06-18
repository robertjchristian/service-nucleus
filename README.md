<div>
<img src="https://travis-ci.org/robertjchristian/service-nucleus.png" />
</div>

<h1>service-nucleus</h1>

Service-Nucleus is largely made up of two core projects, <a href="https://github.com/Netflix/karyon">NetFlix Karyon</a> and <a href="https://github.com/robertjchristian/angular-enterprise-seed">Angular-Enterprise-Seed</a>.

<h3>About</h3>
Service-nucleus is a starting point for developing homogenous, SOA/Enterprise-friendly webservices.  It is a fork of the Netflix/karyon project found on github, with added functionality, examples, and a UI.  Out of the box, this project offers:

* Rich Client-MVC-Based UI built on Angular with: 
 * Twitter Bootstrap and FontAwesome styling
 * Pre-built components (modals, pagination, grids, etc)
 * <i>If you are interested only in the UI, check out <a href="https://github.com/robertjchristian/angular-enterprise-seed">Angular-Enterprise-Seed</a>.</i>
* Java REST webservice development (Jersey stack and working examples)
* Dynamic webservice development (runtime-deployment of Javascript-defined webservices)
* Asynchronous logging (Log4J2)
* Properties management (via Netflix Archaeus)
* JMX monitoring made easy with annotations, and metrics reporting (via Netflix Servo)
* Framework-level monitoring (bytes in/out, CPU high water mark, number of requests handled per endpoint, etc)
* Auditing
 * PCI and Hippa requirements are modeled within the seed framework
 * Custom auditing appender
* Administrative console
 * Always exposes an administrative console on port 8077, regardless of deployment/container configuration.

<h3>Pre-requisites</h3>
* Gradle >= 1.4
* JDK >= 6 
* 
<h3>To run</h3>

* From the project root, "gradle jettyRun"
 * Checkout the admin console at http://localhost:8077
 * Checkout the example REST services at http://localhost:8989/hello-world/v1/
  * /hello - example of static service (no parameters)
  * /hello/to/world - example of dynamic query parameter
 * Checkout the dynamic services landing page
  * localhost:8989/dyn

<h3>To deploy</h3>
* From the project root, "gradle war"
* Then copy (deploy) to your container of choice

<h3>Developing a concrete service from the seed</h3>

There are currently four modules within the seed:

* karyon-admin-web
* karyon-admin
* service-framework
* service-implementation

The first two, for admin, are only coupled to the project as source as an artifact of the karyon project, and have not been moved out because they will likely be modified by this project within the near to mid term.

The service-framework module, like the admin modules, contain the homogenous functionality like auditing, servlet filter exception processing, system/generic metrics, and dynamic services.  Likely, for a given implementation, there will be no need to modify this module.

The service-implementation module is the module everyone will be concerned with.  Out of the box, it defines a hello-world project with two endpoints (one static and one that takes a template parameter), and a simple health check servlet.  This module is meant to be refactored into a useful service implementation.

<h3>Example</h3>

Let's say you wanted to develop a service called "math" that multiplies two template parameters and returns the result.  

<h4>First step, barebones implementation</h4>
* Get/fork the project, (ie) git clone github:robertjchristian/service-nucleus
 * As a sanity check, perform the steps in "to run" outlined above` 
* nano service-implementation/src/main/java/com/liaison/service/HelloworldResource
 * Copy/paste the helloTo service, including annotations
 * Change path from "to/{name}" to "multiply/{a}/{b}"
 * Change method name to multiply, and the single "name" path parameter interface to take a and b "int" parameters instead
 * Change the response.put call to return the value of a*b instead.

That's it!

<h4>Second step, productize</h4>

Realistically you will want to productize your service, which basically means fixing up the namespace from hello-world to something more specific to your particular service.  These steps outline that process.  Note that scaffolding is on the near-term roadmap, and will make most or all of these steps obsolete:

* Edit ./service-implementation/build.gradle
 * Change project.war.basename from "hello-world" to "math"
 * Change System.setProperty("archaius.deployment.applicationId", "hello-world") to System.setProperty("archaius.deployment.applicationId", "math")
* Rename ./service-implementation/src/main/resources/hello-world* to use prefix "math" instead.  
* Refactor your package namespace as desired 
 * Make sure to update math.properties to reflect any high-level namespace change, ie com.netflix.karyon.server.base.packages=com.liaison
to com.netflix.karyon.server.base.packages=com.acme

<h5>Building LESS</h5>
In addition to the prerequisites outlined above, you'll need npm, less, and uglify-js to build Twtter Bootstrap.

<h3>Keeping up with changes</h3>

This project is going to be in flux for the foreseeable future (see roadmap).

Adding the service-nucleus as a remote tracking branch is a low cost and easy way to stay current. It's recommended to do something like:

<pre>
git remote add --track master service-nucleus git@github.com:robertjchristian/service-nucleus.git
git fetch service-nucleus
git merge service-nucleus/master
</pre>

<i>Note that similarly, when developing on this project, a remote tracking branch should be setup against NetFlix/karyon.</i>


