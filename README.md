# common.paths

A simple and small utilities for java.nio.file.Path.

There are many useful libraries in the world, but don't you think that the file sizes of those jar are large?

There is only one class in this library,and the file size of this library's jar is only 13 kb.

## Maven

```xml
<dependency>
  <groupId>com.github.nodamushi</groupId>
  <artifactId>common.paths</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Methods

Please see the [Javadoc](https://nodamushi.github.io/common.paths/) for details.

### null correspondence

Path.getParent() method may return null. This library supplies some methods to avoid NullPointerException.

```java
Path path = Paths.get("a");

path.getParent() ;// null
NPaths.getParent(path); // return empty path
NPaths.resolve(path.getParent(), "b"); // "b"
```

### Utilities for extensions

```java
Path   path  = Paths.get("/etc/test.txt");

String ext   = NPaths.getExtension(path); // "txt"
String fname = NPaths.getNameWithoutExtension(path); // "test"
Path   csv   = NPaths.replaceExtension(path,"csv");// /etc/test.csv
Path   csv0  = NPaths.insertFileName(csv,"_0");//  /etc/test_0.csv
```

### Ignore BOM of UTF-8,UTF-16

```java
try(BufferedReader r = NPaths.newBufferedReader(utfFile,StandardCharset.UTF_8)){
  // If BOM found at the beginning of the file,skip automatically it.
  String line = r.readLine();
}
```


### Simple file walker

```java
// Collect file paths recursively.
List<Path> list = new ArrayList<>();
NPaths.walkFiles( dir, -1 ,(path,attr)->{
  list.add(path);
  return FileVisitResult.CONTINUE;
});
```

### Iterator

```java
Path path = Paths.get("C:/a/b/c.txt");
for(Path p:NPaths.iterator(path)){
  // 1: C:/
  // 2: C:/a
  // 3: C:/a/b
  // 4: C:/a/b/c.txt
  System.out.println(p);
}
```


## LICENSE

Unlicense




