package com.github.nodamushi.common.paths;


import static com.github.nodamushi.common.paths.NPaths.*;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.github.nodamushi.common.paths.NPaths.ItrOption;
import com.github.nodamushi.common.paths.NPaths.Visit;

public class NPathsTest{
  public static boolean isWindows(){
    return System.getProperty("os.name").toLowerCase().startsWith("windows");
  }
  @Test
  public void TestCurrentDirectory(){
    assertNotEquals(getCurrentDirectory(),get("."));
    assertThat(getCurrentDirectory(),is(getCurrentDirectory().normalize()));
    assertTrue(isCurrentDirectory(get(".")));
    assertFalse(isCurrentDirectory(null));
    assertFalse(isCurrentDirectory(get("a/b")));
    assertTrue(isCurrentDirectory(get("")));
    assertTrue(isCurrentDirectory(get(".").toAbsolutePath()));
    assertTrue(isCurrentDirectory(get("a/b/../..")));
  }

  @Test
  public void TestRelativize(){
    assertThat(relativize(get("a/b/c"),get("a/c/d")),is(get("../../c/d")));
    assertThat(relativize(get("/user/bin/"),get("/opt")),is(get("../../opt")));

    if(isWindows()){
      Path current = getCurrentDirectory().getRoot();
      Path another = get(current.toString().toLowerCase().startsWith("d")?"C:/":"D:/");
      assertThat(relativize(get("C:/a/b"),get("C:/a/c")),is(get("../c")));
      assertThat(relativize(get("C:/a/b"),get("D:/a/c")),is(get("D:/a/c")));
      assertThat(relativize(get("a/b"),another.resolve("a/c")),is(another.resolve("a/c")));
      assertThat(relativize(get("a/b"),getCurrentDirectory().resolve("a/c")),is(get("../c")));
    }
  }

  @Test
  public void TestGetParent(){
    assertThat(getParent(get("a/b/c")),is(get("a/b")));
    assertThat(getParent(get("a")),is(get("")));
    if(isWindows()){
      assertThat(getParent(get("C:/a/b")),is(get("C:/a")));
      assertThat(getParent(get("C:/a")),is(get("C:/")));
    }
  }

  @Test
  public void testResolve_Path(){
    assertThat(resolve(null,get("a")),is(get("a")));
    assertThat(resolve(get("."),get("a")),is(get("./a")));
    assertThat(resolve(get("b"),get("a")),is(get("b/a")));
    assertThat(resolve(get("./b"),get("a/c")),is(get("./b/a/c")));
    assertThat(resolve(get("./b"),get("/c/d")),is(get("/c/d")));
    if(isWindows()){
      assertThat(resolve(get("./b"),get("C:/c/d")),is(get("C:/c/d")));
    }
    try{
      resolve(get("a"),(Path)null);
      assertTrue("null pointer exception",false);
    }catch(NullPointerException e){
    }
  }

  @Test
  public void testResolve_String(){
    assertThat(resolve(null,"a"),is(get("a")));
    assertThat(resolve(get("."),"a"),is(get("./a")));
    assertThat(resolve(get("b"),"a"),is(get("b/a")));
    assertThat(resolve(get("./b"),"a\\c"),is(get("./b/a/c")));
    assertThat(resolve(get("./b"),"/a/c"),is(get("/a/c")));
    if(isWindows()){
      assertThat(resolve(get("./b"),"C:/c/d"),is(get("C:/c/d")));
    }
    try{
      resolve(get("a"),(String)null);
      assertTrue("null pointer exception",false);
    }catch(NullPointerException e){
    }
  }

  @Test
  public void testSibling_Path(){
    assertThat(sibling(null,get("a")),is(get("a")));
    assertThat(sibling(get("b"),get("a")),is(get("a")));
    assertThat(sibling(get("/b"),get("a")),is(get("/a")));
    if(isWindows()){
      assertThat(sibling(get("C:/b"),get("a")),is(get("C:/a")));
    }
  }
  @Test
  public void testSibling_String(){
    assertThat(sibling(null,"a"),is(get("a")));
    assertThat(sibling(get("b"),"a"),is(get("a")));
    assertThat(sibling(get("/b"),"a"),is(get("/a")));
    if(isWindows()){
      assertThat(sibling(get("C:/b"),"a"),is(get("C:/a")));
    }
  }

  @Test
  public void testGetFileName(){
    assertThat(getFileName(get("a.txt")),is("a.txt"));
    assertThat(getFileName(get("/a.txt")),is("a.txt"));
    assertThat(getFileName(get("/a/b/c/a.txt")),is("a.txt"));
    assertThat(getFileName(get(".")),is("."));
    assertThat(getFileName(get("/")),is(""));
    if(isWindows()){
      assertThat(getFileName(get("C:/")),is(""));
    }
    try{
      getFileName(null);
      assertTrue("null pointer exception",false);
    }catch(NullPointerException e){
    }
  }
  @Test
  public void testGetExtension(){
    assertThat(getExtension(get("a.txt")),is("txt"));
    assertThat(getExtension(get(".org")),is("org"));
    assertThat(getExtension(get("/a.abc")),is("abc"));
    assertThat(getExtension(get("/a.tar.gz")),is("gz"));
  }

  @Test
  public void testGetExtension_dotCount(){
    assertThat(getExtension(get("/dir/a.b.c.d.e"),-1),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),0),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a"),1),is(""));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),1),is("e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),2),is("d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),3),is("c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),4),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),5),is(""));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),10),is(""));
  }
  @Test
  public void testGetExtension_dotCount2(){
    assertThat(getExtension(get("/dir/a.b.c.d.e"),-1,true),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),0,true),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a"),1,true),is(""));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),1,true),is("e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),2,true),is("d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),3,true),is("c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),4,true),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),5,true),is("b.c.d.e"));
    assertThat(getExtension(get("/dir/a.b.c.d.e"),10,true),is("b.c.d.e"));
  }

  @Test
  public void testGetFileNameWithoutExtension(){
    assertThat(getFileNameWithoutExtension(get("abc.")),is("abc"));
    assertThat(getFileNameWithoutExtension(get("/")),is(""));
    if(isWindows()){
      assertThat(getFileNameWithoutExtension(get("C:/test.txt")),is("test"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz")),is("abc.tar"));
      assertThat(getFileNameWithoutExtension(get("C:\\")),is(""));
    }
  }

  @Test
  public void testGetFileNameWithoutExtension_dotCount(){
    assertThat(getFileNameWithoutExtension(get("abc."),1),is("abc"));
    assertThat(getFileNameWithoutExtension(get("abc."),2),is("abc."));
    assertThat(getFileNameWithoutExtension(get("abc."),0),is("abc"));
    assertThat(getFileNameWithoutExtension(get("/"),1),is(""));
    if(isWindows()){
      assertThat(getFileNameWithoutExtension(get("C:/test.txt"),1),is("test"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),1),is("abc.tar"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),2),is("abc"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),3),is("abc.tar.gz"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),0),is("abc"));
      assertThat(getFileNameWithoutExtension(get("C:\\"),1),is(""));
    }
  }

  @Test
  public void testGetFileNameWithoutExtension_dotCount2(){
    assertThat(getFileNameWithoutExtension(get("abc."),1,true),is("abc"));
    assertThat(getFileNameWithoutExtension(get("abc."),2,true),is("abc"));
    assertThat(getFileNameWithoutExtension(get("abc."),0,true),is("abc"));
    assertThat(getFileNameWithoutExtension(get("/"),1,true),is(""));
    if(isWindows()){
      assertThat(getFileNameWithoutExtension(get("C:/test.txt"),1,true),is("test"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),1,true),is("abc.tar"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),2,true),is("abc"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),3,true),is("abc"));
      assertThat(getFileNameWithoutExtension(get("C:/abc.tar.gz"),0,true),is("abc"));
      assertThat(getFileNameWithoutExtension(get("C:\\"),1,true),is(""));
    }
  }

  @Test
  public void testRemoveExtension(){
    assertThat(removeExtension(get("/a/test.txt")),is(get("/a/test")));
    assertThat(removeExtension(get("a/b/c.tar.gz")),is(get("a/b/c.tar")));
    assertThat(removeExtension(get("a.")),is(get("a")));
    assertThat(removeExtension(get(".")),is(get("")));
    assertThat(removeExtension(get("..")),is(get(".")));
    assertThat(removeExtension(get("/")),is(get("/")));
    assertThat(removeExtension(get("")),is(get("")));
    if(isWindows()){
      assertThat(removeExtension(get("C:/a/test.txt")),is(get("C:/a/test")));
      assertThat(removeExtension(get("C:\\")),is(get("C:/")));
    }
  }

  @Test
  public void testRemoveExtension_dotCount(){
    assertThat(removeExtension(get("a/b/abc."),1),is(get("a/b/abc")));
    assertThat(removeExtension(get("/abc."),2),is(get("/abc.")));
    assertThat(removeExtension(get("abc."),0),is(get("abc")));
    assertThat(removeExtension(get("/"),1),is(get("/")));
    assertThat(removeExtension(get("/"),0),is(get("/")));
    assertThat(removeExtension(get(""),0),is(get("")));
    assertThat(removeExtension(get(""),1),is(get("")));
    if(isWindows()){
      assertThat(removeExtension(get("C:/test.txt"),1),is(get("C:/test")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),1),is(get("C:/abc.tar")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),2),is(get("C:/abc")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),3),is(get("C:/abc.tar.gz")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),0),is(get("C:/abc")));
      assertThat(removeExtension(get("C:\\"),0),is(get("C:/")));
      assertThat(removeExtension(get("C:\\"),1),is(get("C:/")));
    }
  }


  @Test
  public void testRemoveExtension_dotCount2(){
    assertThat(removeExtension(get("a/b/abc."),1,true),is(get("a/b/abc")));
    assertThat(removeExtension(get("/c/abc."),2,true),is(get("/c/abc")));
    assertThat(removeExtension(get("abc."),0,true),is(get("abc")));
    assertThat(removeExtension(get("/"),1,true),is(get("/")));
    assertThat(removeExtension(get("/"),0,true),is(get("/")));
    assertThat(removeExtension(get(""),0,true),is(get("")));
    assertThat(removeExtension(get(""),1,true),is(get("")));
    if(isWindows()){
      assertThat(removeExtension(get("C:/test.txt"),1,true),is(get("C:/test")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),1,true),is(get("C:/abc.tar")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),2,true),is(get("C:/abc")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),3,true),is(get("C:/abc")));
      assertThat(removeExtension(get("C:/abc.tar.gz"),0,true),is(get("C:/abc")));
      assertThat(removeExtension(get("C:\\"),1,true),is(get("C:/")));;
      assertThat(removeExtension(get("C:\\"),0,true),is(get("C:/")));;
    }
  }

  @Test
  public void testNewFileName(){
    assertThat(newFileName(get("/NAME.TXT"),"pre_","_ins","org","_post",1,false),is(get("/pre_NAME_ins.org_post")));
    assertThat(newFileName(get("/NAME.TXT"),"pre_","_ins",null,"_post",1,false),is(get("/pre_NAME_ins.TXT_post")));
    assertThat(newFileName(get("NAME.TXT"),"pre_","_ins","","_post",1,false),is(get("pre_NAME_ins_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",0,false),is(get("/a/pre_b_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",1,false),is(get("/a/pre_b.c.d.e_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",2,false),is(get("/a/pre_b.c.d_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",3,false),is(get("/a/pre_b.c_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",4,false),is(get("/a/pre_b_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",5,false),is(get("/a/pre_b.c.d.e.f_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",0,true),is(get("/a/pre_b_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",1,true),is(get("/a/pre_b.c.d.e_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",2,true),is(get("/a/pre_b.c.d_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",3,true),is(get("/a/pre_b.c_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",4,true),is(get("/a/pre_b_ins.txt_post")));
    assertThat(newFileName(get("/a/b.c.d.e.f"),"pre_","_ins","txt","_post",5,true),is(get("/a/pre_b_ins.txt_post")));
    assertThat(newFileName(get(""),null,null,null,null,1,false),is(get("")));
    assertThat(newFileName(get("/a"),null,null,null,null,1,false),is(get("/a")));
    assertThat(newFileName(get("/a"),"pre_",null,null,null,1,false),is(get("/pre_a")));
    assertThat(newFileName(get("/a"),"pre_",null,null,"_post",1,false),is(get("/pre_a_post")));
    assertThat(newFileName(get("/a"),"pre_","_ins",null,"_post",1,false),is(get("/pre_a_ins_post")));
    assertThat(newFileName(get("/a"),"pre_",null,"txt","_post",1,false),is(get("/pre_a.txt_post")));
    assertThat(newFileName(get("/a.b"),"pre_",null,"","_post",1,false),is(get("/pre_a_post")));
    assertThat(newFileName(get(""),"pre_",null,"","_post",1,false),is(get("pre__post")));
    assertThat(newFileName(get("/"),null,"ins","",null,1,false),is(get("/ins")));
    if(isWindows()){
      assertThat(newFileName(get("C:/"),"pre_",null,"","_post",1,false),is(get("C:/pre__post")));
      assertThat(newFileName(get("C:/"),null,"ins","",null,1,false),is(get("C:/ins")));
    }
  }

  @Test
  public void testReplaceExtension(){
    assertThat(replaceExtension(get("/a/b/cde.a"),null),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("cde.a.d"),""),is(get("cde.a")));
    assertThat(replaceExtension(get("/cde.a"),"txt"),is(get("/cde.txt")));
    assertThat(replaceExtension(get("/cde"),"txt"),is(get("/cde.txt")));
    assertThat(replaceExtension(get("cde.a"),".txt"),is(get("cde.txt")));
    assertThat(replaceExtension(get(""),".txt"),is(get(".txt")));
    assertThat(replaceExtension(get("."),".org"),is(get(".org")));
    assertThat(replaceExtension(get("/"),".txt"),is(get("/.txt")));
    if(isWindows()){
      assertThat(replaceExtension(get("C:/"),".txt"),is(get("C:/.txt")));
    }
  }

  @Test
  public void testReplaceExtension_dotCount(){
    assertThat(replaceExtension(get("/a/b/cde.a"),null,0),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("cde.a"),null,1),is(get("cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"",1),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"txt",1),is(get("/a/b/cde.txt")));
    assertThat(replaceExtension(get("/a/b/cde.a"),null,2),is(get("/a/b/cde.a")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"",2),is(get("/a/b/cde.a")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"txt",2),is(get("/a/b/cde.a.txt")));
    assertThat(replaceExtension(get(""),".txt",0),is(get(".txt")));
    assertThat(replaceExtension(get(""),".txt",2),is(get(".txt")));
    assertThat(replaceExtension(get("."),".org",0),is(get(".org")));
    assertThat(replaceExtension(get("."),".org",1),is(get(".org")));
    assertThat(replaceExtension(get("/"),".txt",0),is(get("/.txt")));
    assertThat(replaceExtension(get("/"),".txt",1),is(get("/.txt")));
    if(isWindows()){
      assertThat(replaceExtension(get("C:/"),".txt",0),is(get("C:/.txt")));
      assertThat(replaceExtension(get("C:/"),".txt",1),is(get("C:/.txt")));
    }
  }
  @Test
  public void testReplaceExtension_dotCount2(){
    assertThat(replaceExtension(get("/a/b/cde.a"),null,0,true),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("cde.a"),null,1,true),is(get("cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"",1,true),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"txt",1,true),is(get("/a/b/cde.txt")));
    assertThat(replaceExtension(get("/a/b/cde.a"),null,2,true),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"",2,true),is(get("/a/b/cde")));
    assertThat(replaceExtension(get("/a/b/cde.a"),"txt",2,true),is(get("/a/b/cde.txt")));
    assertThat(replaceExtension(get(""),".txt",0,true),is(get(".txt")));
    assertThat(replaceExtension(get(""),".txt",2,true),is(get(".txt")));
    assertThat(replaceExtension(get("."),".org",0,true),is(get(".org")));
    assertThat(replaceExtension(get("."),".org",1,true),is(get(".org")));
    assertThat(replaceExtension(get("/"),".txt",0,true),is(get("/.txt")));
    assertThat(replaceExtension(get("/"),".txt",1,true),is(get("/.txt")));
    if(isWindows()){
      assertThat(replaceExtension(get("C:/"),".txt",0,true),is(get("C:/.txt")));
      assertThat(replaceExtension(get("C:/"),".txt",1,true),is(get("C:/.txt")));
    }
  }

  @Test
  public void testPrependFileName(){
    assertThat(prependFileName(get("/"),"pre_"),is(get("/pre_")));
    assertThat(prependFileName(get("/a"),"pre_"),is(get("/pre_a")));
    assertThat(prependFileName(get("a"),"pre_"),is(get("pre_a")));
    assertThat(prependFileName(get("/a/b/"),"pre_"),is(get("/a/pre_b")));
    assertThat(prependFileName(get("/a/b/c"),"pre_"),is(get("/a/b/pre_c")));
    if(isWindows()){
      assertThat(prependFileName(get("C:\\"),"pre_"),is(get("C:/pre_")));
    }
  }

  @Test
  public void testAppendFileName(){
    assertThat(appendFileName(get("/"),"post"),is(get("/post")));
    assertThat(appendFileName(get("/a"),"post"),is(get("/apost")));
    assertThat(appendFileName(get("a"),"post"),is(get("apost")));
    assertThat(appendFileName(get("/a/b/"),"post"),is(get("/a/bpost")));
    assertThat(appendFileName(get("/a/b/c"),"post"),is(get("/a/b/cpost")));
    if(isWindows()){
      assertThat(appendFileName(get("C:\\"),"post"),is(get("C:/post")));
    }
  }

  @Test
  public void testInsertFileName(){
    assertThat(insertFileName(get("/a.txt"),"_ins",10,true),is(get("/a_ins.txt")));
    assertThat(insertFileName(get("/a.txt"),"_ins",10,false),is(get("/a.txt_ins")));
    assertThat(insertFileName(get("/a.txt"),"_ins",10),is(get("/a.txt_ins")));
    assertThat(insertFileName(get("/a.txt"),"_ins"),is(get("/a_ins.txt")));
    assertThat(insertFileName(get("/a.b.c"),"_ins",1,true),is(get("/a.b_ins.c")));
    assertThat(insertFileName(get("/a.b.c"),"_ins",2,true),is(get("/a_ins.b.c")));
    assertThat(insertFileName(get("/a.b.c"),"_ins",1),is(get("/a.b_ins.c")));
    assertThat(insertFileName(get("/a.b.c"),"_ins",2),is(get("/a_ins.b.c")));
  }

  private static Path RESOURCES=get("src/test/resources");
  @Test
  public void testNewBufferedReader()throws Exception{
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("empty.txt"),StandardCharsets.UTF_8)){
      assertNull(r.readLine());
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("noBOM.txt"),StandardCharsets.UTF_8)){
      assertThat(r.readLine(),is("aiueo"));
    }

    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("withBOM.txt"),StandardCharsets.UTF_8)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("withBOM16BE.txt"),StandardCharsets.UTF_16BE)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("noBOM16BE.txt"),StandardCharsets.UTF_16BE)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("withBOM16BE.txt"),StandardCharsets.UTF_16)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("noBOM16BE.txt"),StandardCharsets.UTF_16)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("withBOM16LE.txt"),StandardCharsets.UTF_16LE)){
      assertThat(r.readLine(),is("aiueo"));
    }
    try(BufferedReader r=newBufferedReader(RESOURCES.resolve("noBOM16LE.txt"),StandardCharsets.UTF_16LE)){
      assertThat(r.readLine(),is("aiueo"));
    }

    if(Charset.forName("shift-jis")!=null){
      try(BufferedReader r=newBufferedReader(RESOURCES.resolve("sjis.txt"),Charset.forName("shift-jis"))){
        assertThat(r.readLine(),is("あいうえお"));
      }
    }

    if(Charset.forName("euc-jp")!=null){
      try(BufferedReader r=newBufferedReader(RESOURCES.resolve("eucjp.txt"),Charset.forName("euc-jp"))){
        assertThat(r.readLine(),is("あいうえお"));
      }
    }
  }
  @Test
  public void testWalkFiles() throws IOException{
    List<Path> list=new ArrayList<>();
    Visit f = (file,attrs)->{
      list.add(file);
      return FileVisitResult.CONTINUE;
    };
    walkFiles(RESOURCES,-1,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/b/c/d/t1.txt"),RESOURCES.resolve("a/b/c/d/t2.txt"),
        RESOURCES.resolve("a/b/c/t1.txt"),RESOURCES.resolve("a/b/c/t2.txt"),
        RESOURCES.resolve("a/b/c/x/t1.txt"),RESOURCES.resolve("a/b/c/x/t2.txt"),
        RESOURCES.resolve("a/b/t1.txt"), RESOURCES.resolve("a/b/t2.txt"),
        RESOURCES.resolve("a/t1.txt"), RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));


    list.clear();

    walkFiles(RESOURCES,0,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();


    walkFiles(RESOURCES,1,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/t1.txt"), RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();

    walkFiles(RESOURCES,2,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/b/t1.txt"), RESOURCES.resolve("a/b/t2.txt"),
        RESOURCES.resolve("a/t1.txt"),RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();

    walkFiles(RESOURCES,3,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/b/c/t1.txt"),RESOURCES.resolve("a/b/c/t2.txt"),
        RESOURCES.resolve("a/b/t1.txt"), RESOURCES.resolve("a/b/t2.txt"),
        RESOURCES.resolve("a/t1.txt"), RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();


    walkFiles(RESOURCES,4,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/b/c/d/t1.txt"),RESOURCES.resolve("a/b/c/d/t2.txt"),
        RESOURCES.resolve("a/b/c/t1.txt"),RESOURCES.resolve("a/b/c/t2.txt"),
        RESOURCES.resolve("a/b/c/x/t1.txt"),RESOURCES.resolve("a/b/c/x/t2.txt"),
        RESOURCES.resolve("a/b/t1.txt"), RESOURCES.resolve("a/b/t2.txt"),
        RESOURCES.resolve("a/t1.txt"), RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();

    walkFiles(RESOURCES,100,f);
    assertThat(list,Matchers.contains(
        RESOURCES.resolve("a/b/c/d/t1.txt"),RESOURCES.resolve("a/b/c/d/t2.txt"),
        RESOURCES.resolve("a/b/c/t1.txt"),RESOURCES.resolve("a/b/c/t2.txt"),
        RESOURCES.resolve("a/b/c/x/t1.txt"),RESOURCES.resolve("a/b/c/x/t2.txt"),
        RESOURCES.resolve("a/b/t1.txt"), RESOURCES.resolve("a/b/t2.txt"),
        RESOURCES.resolve("a/t1.txt"), RESOURCES.resolve("a/t2.txt"),
        RESOURCES.resolve("empty.txt"),
        RESOURCES.resolve("eucjp.txt"),
        RESOURCES.resolve("noBOM.txt"),
        RESOURCES.resolve("noBOM16BE.txt"),
        RESOURCES.resolve("noBOM16LE.txt"),
        RESOURCES.resolve("sjis.txt"),
        RESOURCES.resolve("withBOM.txt"),
        RESOURCES.resolve("withBOM16BE.txt"),
        RESOURCES.resolve("withBOM16LE.txt")));
    list.clear();
  }

  @Test
  public void testWalkDirectories() throws IOException{
    List<Path> list=new ArrayList<>();
    Visit f = (file,attrs)->{
      list.add(file);
      return FileVisitResult.CONTINUE;
    };
    walkDirectories(RESOURCES,-1,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b"),
        RESOURCES.resolve("a/b/c"),
        RESOURCES.resolve("a/b/c/d"),
        RESOURCES.resolve("a/b/c/x")
        ));
    list.clear();


    walkDirectories(RESOURCES,0,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a")
        ));
    list.clear();

    walkDirectories(RESOURCES,1,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b")
        ));
    list.clear();

    walkDirectories(RESOURCES,2,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b"),
        RESOURCES.resolve("a/b/c")
        ));
    list.clear();

    walkDirectories(RESOURCES,3,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b"),
        RESOURCES.resolve("a/b/c"),
        RESOURCES.resolve("a/b/c/d"),
        RESOURCES.resolve("a/b/c/x")
        ));
    list.clear();

    walkDirectories(RESOURCES,4,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b"),
        RESOURCES.resolve("a/b/c"),
        RESOURCES.resolve("a/b/c/d"),
        RESOURCES.resolve("a/b/c/x")
        ));
    list.clear();
    walkDirectories(RESOURCES,100,f);
    assertThat(list,Matchers.contains(
        RESOURCES,
        RESOURCES.resolve("a"),
        RESOURCES.resolve("a/b"),
        RESOURCES.resolve("a/b/c"),
        RESOURCES.resolve("a/b/c/d"),
        RESOURCES.resolve("a/b/c/x")
        ));
    list.clear();

  }

  @Test
  public void testIteratorNameOnly(){
    {
      int i=0;
      String[] ret = {"a","b","c","d"};
      for(Path p:iterator(get("a/b/c/d"),ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
    {
      int i=0;
      String[] ret = {"/","a","b","c","d"};
      for(Path p:iterator(get("/a/b/c/d"),ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }

    if(isWindows()){
      int i=0;
      String[] ret = {"C:/","a","b","c","d"};
      for(Path p:iterator(get("C:/a/b/c/d"),ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
  }

  @Test
  public void testIteratorExistOnly(){
    int i=0;
    String[] ret = {
        "src",
        "src/test",
        "src/test/resources",
        "src/test/resources/a",
    };
    for(Path p:iterator(get("src/test/resources/a/z/k/d.txt"),ItrOption.EXIST_ONLY)){
      assertThat(p,is(get(ret[i++])));
    }
    assertThat(i,is(ret.length));
  }

  @Test
  public void testIteratorFullPath(){
    {
      int i=0;
      String[] ret = {"a","a/b","a/b/c","a/b/c/d"};
      for(Path p:iterator(get("a/b/c/d"),ItrOption.FULL_PATH)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
    {
      int i=0;
      String[] ret = {"/","/a","/a/b","/a/b/c","/a/b/c/d"};
      for(Path p:iterator(get("/a/b/c/d"),ItrOption.FULL_PATH)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }

    if(isWindows()){
      int i=0;
      String[] ret = {"C:/","C:/a","C:/a/b","C:/a/b/c","C:/a/b/c/d"};
      for(Path p:iterator(get("C:/a/b/c/d"),ItrOption.FULL_PATH)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
  }


  @Test
  public void testIteratorStartIndex(){
    {
      int i=0;
      String[] ret = {"c","d"};
      for(Path p:iterator(get("a/b/c/d"),2,ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
    {
      int i=0;
      String[] ret = {"a","b","c","d"};
      for(Path p:iterator(get("/a/b/c/d"),1,ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }

    if(isWindows()){
      int i=0;
      String[] ret = {"a","b","c","d"};
      for(Path p:iterator(get("C:/a/b/c/d"),1,ItrOption.NAME_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
    {
      int i=0;
      String[] ret = {
          "src/test/resources",
          "src/test/resources/a",
      };
      for(Path p:iterator(get("src/test/resources/a/z/k/d.txt"),2,ItrOption.EXIST_ONLY)){
        assertThat(p,is(get(ret[i++])));
      }
      assertThat(i,is(ret.length));
    }
  }


  @Test
  public void testStream(){
    assertThat(stream(get("/a/b/c/d"),ItrOption.NAME_ONLY).collect(toList()),
        Matchers.contains(get("/"),get("a"),get("b"),get("c"),get("d")));
  }

  @Test
  public void testForEach(){
    List<Path> list=new ArrayList<>();
    forEach(get("/a/b/c/d"),ItrOption.NAME_ONLY,list::add);
    assertThat(list,
        Matchers.contains(get("/"),get("a"),get("b"),get("c"),get("d")));
  }

}
