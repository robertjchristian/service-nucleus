FS2
====
[Flexible Storage System](http://robertjchristian.github.com/fs2) - Middleware Object Storage API.  Store your data anywhere, and move it at any time, with no client code impact.

What is FS2?
============================
FS2 allows you to manage arbitrary data objects using familiar operations like create, read, update, and delete. Using a simple interface that abstracts the underlying persistence details, applications can interact with their data in a way similar to using
a terminal to interact with a local filesystem. Behind the curtains, a storage provider does the heavy lifting, be it integrating with a cloud storage provider like Google Cloud Storage
or Amazon S3, a Mongo NoSQL database, a fileshare, or even MemCache.

Why FS2?
============================
There are similar technologies out there such as Apache Commons Virtual File System (VFS) and Apache Jackrabbit.  FS2 distinguishes itself with the following:
* Storage-agnostic API.  Client code does not know about the underlying persistence store.  For example, a VFS URL make look like jar:/a/b, where fs2 is simply fs2:/a/b/.
* TDD friendly.  Use the default FS2 repo (in-memory) while developing for easy testing without minding the complexities of configuration, authentication, and details of databases/filesystem stores.  Then when the code is ready for prime time, simply flip a switch (ie change "mem" to "mongo"), and objects will be persisted.
* Lightweight dependencies.  The core FS2 API code is lightweight, and for any given deployment scenario, you need only to include the concrete repository that will be used.
* Built-in tests.  It's easy to have confidence in a new concrete repository implementation when you can plug it right into an existing test framework.
* Less config.  By default, FS2 stores objects in memory, and there is zero configuration required.
* Easier config.  FS2 is not going to require heaps of XML files defining factories in order to work.  Just override the default values you wish to change, in code or by providing a properties file in json format.
* Leaves the typical "heavyweightness" of Java frameworks behind.  IE uses json for config and object descriptors, relies on default values so the only existing configuration is override configuration.

Prerequisites
============================
* JDK6 or greater.

How to build
============================

With Ant
====
from fs2, run "ant"

In Eclipse
===
_The fs2 module is committed to git with Eclipse project settings (.project/.classpath) at the root.  So getting started is as easy as_
File->Import->General->Existing Projects into Workspace->Next->Select Root Directory-Browse->Select fs2

Tests
============================
See that the unit test structure mirrors the src structure.
<pre>
tests
└── com
    └── fs2
        ├── api
        │   ├── AbstractAPITest.java
        │   └── MetadataSerializationTest.java
        └── storage
            ├── file
            │   └── FileAPITest.java
            └── memory
                └── MemoryAPITest.java
</pre>
The AbstractAPITest is meant to serve as a functional-level test of the API, exercising CRUD, tree manipulations, and list functionality.  For each concrete implementation, simply extend the base test to make sure it is included.  For example, the concrete FileAPITest looks like this:
<pre>
public class FileAPITest extends AbstractAPITest {
  @Override
  protected FlexibleStorageSystem getFS2Instance() {
    FS2Configuration config = new FS2DefaultFileConfig();
    return FS2Factory.newInstance(config);
  }
}
</pre>
This makes it simple to add new concrete storage repo implementations within the test suite.

Roadmap
============================

* Allow use of regular expressions in assertions.
* Provide richer test examples.  This most likely means building a web app that authenticates, and examines headers and payloads.
* Add in support for spawning many tests instances at a time to facilitate stress testing.
* Allow use of tokens in metadata files for greater flexibility.  For example url could contain http://${host}${port}/foo/bar and the values for host and port could be passed in on the command line or via properties file.  This would work the same way for payloads, headers, and assertions.
* Consider certificate support for SSL. (currently no client cert support, and always trusts host cert)
* Consider building a set of penetration tests based on OWASP.
* Consider removing requests in favor of urllib2 so that there are no prerequisites other than Python.

Support
============================
* Report issues [here] (https://github.com/robertjchristian/fs2/issues)
* [Wiki] (https://github.com/robertjchristian/fs2/wiki)
* Also see the [fs2 project page] (http://robertjchristian.github.com/fs2)
