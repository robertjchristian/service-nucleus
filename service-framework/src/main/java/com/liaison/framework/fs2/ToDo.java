package com.liaison.framework.fs2;
/**
 * 
 * FS2 To Do List: 
 *
 * TODO Consider adding support for encryption and zip of payloads
 * 
 * TODO Move configuration properties to JSON
 * 
 * TODO Ability to override bootstrapped (default) location, (ie) if fs2 was
 * bootstrapped as in memory, fetching with url fs2://a/b/c?store=part suggests
 * to fs2 to look at the file repo instead
 * 
 * TODO consider using Apache Commons VFS
 * http://commons.apache.org/vfs/filesystems.html to create concrete
 * repositories for FTP/S, HTTP/S, SFTP and WebDAV
 * 
 * TODO Check before throwing so that we aren't wrapping with the same type.
 * 
 * TODO In general, should catch as early as possible and rethrow with
 * persistence-agnostic unchecked exception. To be clear we don't want to lose
 * the persistence specific exception, so put it into the FS2Exception that
 * propagates outside of the API but as an additional member variable, not part
 * of the exception. So (ie) e.getLocalizedMessage() would be "Object not found"
 * and e.getCause() would return FileNotFoundException.
 * 
 * TODO Move payload size back into meta.
 * 
 * TODO Make sure close on inputstreams updates the meta.
 * 
 * TODO Stress, load, and concurrency test.
 * 
 * TODO meta to json not printing out dates, payload size, etc
 * 
 * TODO Combine validate uri with ensure no trailing slash, and make sure it is
 * called first on every call to this object to vet uri syntax and
 * normalization.
 * 
 * TODO Along with above, check to see whether a repo other than the default
 * repo needs to be used. Note that meta will need to contain the override so
 * the round robin will persist to the right store when overriding.
 * 
 * TODO Rules for node names... should be lowest common denominator. In other
 * words, create entry at the API level needs to make sure that only letters and
 * numbers are used (maybe dots) for name. This will keep it platform
 * independent.
 * 
 **/

public interface ToDo {
  // This class contains JavaDoc comments for high level/roadmap TO DO's...
}
