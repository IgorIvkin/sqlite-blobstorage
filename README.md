# SQLite Blob Storage

### Introduction
This application presents a *Spring Boot*-backed microservice that is intended to store
files of any type in the blob storage. Blob storage itself is based on **SQLite**. It supports
multiple binary volumes that internally presented in the form of multiple SQLite database
files.

### How it works in depth
* The user interacts with front-end &mdash; service `BlobStorage`. Front-end provides the methods
to store and retrieve the files
* Once file is to be stored the front-end checks for possible restrictions (e.g. mime-type
of file, size in bytes etc). The file is stored inside of one of the blob volumes.
* Each blob volume is a SQLite database. The system supports
restriction to the maximal size in bytes for every volume file. 
Once this limitation reached the new blob volume file will be instantiated.
* In case if size of blob volume is changing (for example after `VACUUM` operation) the system
is able to use available space again.
* A stored file has **an address** that contains two elements: 
    1) ID of blob volume
    2) ID of item inside this volume
* You have to store this information somewhere. Otherwise, you can't get your file!
* You are provided by a very simple RESTful API to store and get the file. Consider this API
as learning example mostly. It is recommended to expand and adapt the code to your needs.

### Configurable values
Configuration file is a JSON-file located in `config\blobstorage.json`.
* `maxBlobVolumeSize` &mdash; a maximal blob volume size in **megabytes**. Once blob volume reaches this
limitation then new blob volume will be created;
* `maxBlobItemSize` - a maximal size of file is possible to store in **megabytes**, the bigger file will be rejected;
* `allowToCreateNewVolumes` - if `true` allows to create new volumes when `maxBlobVolumeSize` hits its limit;
* `allowedMimeTypes` - an array of allowed mime-types, all the other mime-types will be rejected.

### TODOs
The project was written primarily to validate the hypothesis. While it's applicable
there is a lot of things to improve.
1) add possibility to delete the stored file
2) add more tests
3) handle more exceptions and provide more consistent API