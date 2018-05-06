package nodamushi.com.github.common.paths;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * nodamushi path utilities.
 * @author nodamushi
 * @version 1.0.0
 */
public class NPaths{
  /**
   * {@link FileSystem#getPath(String, String...)}
   * @param fileSystem nullable.
   * @param first nullable.if <code>first</code> is <code>null</code>,an empty string is used.
   * @param more nullable.more path
   * @return if fileSystem is <code>null</code>,return <code>Paths.get(first,more...)</code>
   * @see FileSystem#getPath(String, String...)
   * @since 1.0.0
   */
  public static Path get(FileSystem fileSystem,String first,String... more){
    return fileSystem==null?Paths.get(first,more):fileSystem.getPath(first==null?"":first,more==null?new String[]{}:more);
  }

  private static Path get(Path fileSystem,String first,String... more){
    return get(fileSystem==null?FileSystems.getDefault():fileSystem.getFileSystem(),first,more);
  }

  /**
   * get the current work directory name.<br>
   * @return <code>getCurrentDirectory().getFileName()</code>
   * @since 1.0.0
   */
  public static Path getCurrentDirectoryName(){
    return getCurrentDirectory().getFileName();
  }

  /**
   * get the current work directory absolute path.
   * @return <code>Paths.get(".").toAbsolutePath().normalize()</code>
   * @since 1.0.0
   */
  public static Path getCurrentDirectory(){
    return Paths.get("").toAbsolutePath();
  }

  /**
   * <code> getCurrentDorectory().equals(path.toAbsolutePath()) </code>
   * @param path path
   * @return <code> getCurrentDorectory().equals(path.toAbsolutePath()) </code>
   * @since 1.0.0
   */
  public static boolean isCurrentDirectory(Path path){
    return path!=null && getCurrentDirectory().equals(path.toAbsolutePath().normalize());
  }

  /**
   * get a relative path.
   * If roots of <code>basePath</code> and <code>path</code> are different,return <code>path</code>.toAbsolutePath().
   * <ul>
   * <li>"a/b/c" , "a/c/d" → "../../c/d"</li>
   * <li>"C:/a/b", "C:/a/c" → "../c"</li>
   * <li>"C:/a/b", "D:/a/c" → "D:/a/c"</li>
   * <li>"a/b", "D:/a/c" → "D:/a/c" (* the root of the current work directory is NOT D:)</li>
   * <li>"a/b", "D:/a/c" → relative from "{current work directory}/a/b" (* the root of the current work directory is D:)</li>
   * </ul>
   * @param basePath base path.non null.
   * @param path path.non null
   * @return relative path from <code>basePath</code>.
   * @throws NullPointerException arguments are <code>null</code>.
   * @since 1.0.0
   */
  public static Path relativize(Path basePath,Path path)throws NullPointerException{
    if(basePath.isAbsolute()){
      Path ap = path.toAbsolutePath();
      return (basePath.getRoot().equals(ap.getRoot()))?basePath.relativize(ap):ap;
    }else{
      if(!path.isAbsolute()){
        return basePath.relativize(path);
      }
      Path ab = basePath.toAbsolutePath();
      return (ab.getRoot().equals(path.getRoot()))?ab.relativize(path):path;
    }
  }

  /**
   * get a parent of <code>path</code>.
   * if <code>path</code> has no parent,return an empty path.(never return <code>null</code>)
   * @param path path.non null.
   * @return <code>parent.getParent()</code> or <code>path.getFileSystem().getPath("").</code>
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path getParent(Path path)throws NullPointerException{
    Path parent = path.getParent();
    return parent == null? path.getFileSystem().getPath(""):parent;
  }

  /**
   * get <code>parent/path</code>
   * @param parent nullable
   * @param path non null
   * @return <code>parent/path</code>. If <code>path</code> is an absolute path,return <code>path</code>.
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path resolve(Path parent,Path path)throws NullPointerException{
    requireNonNull(path,"path is null");
    return parent==null||path.isAbsolute()?path:parent.resolve(path);
  }

  /**
   * get <code>parent/path</code>
   * @param parent nullable
   * @param path non null
   * @return <code>parent/path</code>.If <code>path</code> is an absolute path,return <code>path</code>.
   * When <code>parent</code> is <code>null</code>,the FileSystem of the returned path is <code>FileSystems.getDefault()</code>.
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path resolve(Path parent,String path)throws NullPointerException{
    return resolve(parent,get(parent,requireNonNull(path,"path is <code>null</code>")));
  }

  /**
   * get <code>(sibling.getParent())/path</code>
   * @param sibling nullable
   * @param path non null
   * @return <code>(sibling.getparent())/path</code>.If <code>sibling</code> is <code>null</code> or <code>path</code> is an absolute path,return <code>path</code>.
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path sibling(Path sibling,Path path)throws NullPointerException{
    requireNonNull(path,"path is null");
    return sibling==null || path.isAbsolute()?path:sibling.resolveSibling(path);
  }

  /**
   * get <code>(sibling.getParent())/path</code>
   * @param sibling nullable
   * @param path non null
   * @return <code>(sibling.getparent())/path</code>.If <code>sibling</code> is <code>null</code> or <code>path</code> is an absolute path,return <code>path</code>.
   * When <code>sibling</code> is <code>null</code>,the FileSystem of the returned path is <code>FileSystems.getDefault()</code>.
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path sibling(Path sibling,String path)throws NullPointerException{
    return sibling(sibling,get(sibling,requireNonNull(path,"path is null")));
  }

  /**
   * get string of the file name of the path.
   * <ul>
   * <li>"test.txt" → "test.txt"</li>
   * <li>"a/b/c/test.txt" → "test.txt"</li>
   * <li>"/" → ""</li>
   * <li>"C:/" → ""</li>
   * <li>"." → "."</li>
   * </ul>
   * @param path non null.
   * @return path.getFileName().toString()
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getFileName(Path path)throws NullPointerException{
    Path p =path.getFileName();
    return p==null? "":p.toString();
  }

  private static int indexOfExtension(Path path,int dotCount,boolean fuzzy)
      throws NullPointerException{
    String name=getFileName(path);
    if(dotCount==1){
      return name.lastIndexOf('.');
    }
    int lastIndex = name.length();

    int end = dotCount==0?-1:dotCount;
    for(int i=0;i!=end;i++){
      int index=name.lastIndexOf('.',lastIndex-1);
      if(index == -1){
        return i==0 || (!fuzzy && end > 0)? -1:lastIndex;
      }
      lastIndex = index;
    }
    return lastIndex;
  }

  /**
   * get the file extension.
   * <ul>
   * <li>"C:/test.txt" → "txt"</li>
   * <li>"abc.tar.gz" → "gz"</li>
   * <li>"abc." → ""</li>
   * <li>"/" → ""</li>
   * </ul>
   * @param path non null.
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getExtension(Path path)throws NullPointerException{
    String name=getFileName(path);
    int index=name.lastIndexOf('.');
    return index==-1?"":name.substring(index+1);
  }
  /**
   * get the file extension.<br>
   * <ul>
   * <li>"/dir/a.tar.gz",1 → "gz"</li>
   * <li>"/dir/a.tar.gz",2 → "tar.gz"</li>
   * <li>"/dir/a.b.c.d.e",0 → "b.c.d.e"</li>
   * </ul>
   * @param path non null.
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method returns the longest extension.
   * @param fuzzy if true,the count of the number of "." is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",1 → "e"</li>
   * <li>"/dir/a.b.c.d.e",2 → "d.e"</li>
   * <li>"/dir/a.b.c.d.e",4 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",5 → ""</li>
   * <li>"/dir/a.b.c.d.e",10 → ""</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",1 → "e"</li>
   * <li>"/dir/a.b.c.d.e",2 → "d.e"</li>
   * <li>"/dir/a.b.c.d.e",4 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",5 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",10 → "b.c.d.e"</li>
   * </ul>
   *
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getExtension(Path path,int dotCount,boolean fuzzy)
      throws NullPointerException{
    String name=getFileName(path);
    int index = indexOfExtension(path,dotCount,fuzzy);
    return index==-1?"":name.substring(index+1);
  }
  /**
   * get the file extension.
   * <ul>
   * <li>"/dir/a.tar.gz",1 → "gz"</li>
   * <li>"/dir/a.tar.gz",2 → "tar.gz"</li>
   * <li>"/dir/a.b.c.d.e",0 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",1 → "e"</li>
   * <li>"/dir/a.b.c.d.e",2 → "d.e"</li>
   * <li>"/dir/a.b.c.d.e",4 → "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",5 → ""</li>
   * </ul>
   * @param path non null.
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method returns the longest extension.
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @see #getExtension(Path, int, boolean)
   * @since 1.0.0
   */
  public static String getExtension(Path path,int dotCount)throws NullPointerException{
    return getExtension(path,dotCount,false);
  }


  /**
   * get the file name without the extension.
   * <ul>
   * <li>"C:/test.txt" → "test"</li>
   * <li>"abc.tar.gz" → "abc.tar"</li>
   * <li>"abc." → "abc"</li>
   * <li>"/" → ""</li>
   * </ul>
   * @param path non null.
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getFileNameWithoutExtension(Path path)throws NullPointerException{
    String name=getFileName(path);
    int index=name.lastIndexOf('.');
    return index==-1?name:name.substring(0,index);
  }

  /**
   * get the file name without the extension.<br>
   * <ul>
   * <li>"/dir/a.tar.gz",1 → "a.tar"</li>
   * <li>"/dir/a.tar.gz",2 → "a"</li>
   * <li>"/dir/a.b.c.d.e",0 → "a"</li>
   * </ul>
   * @param path non null.
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method removes the longest extension.
   * @param fuzzy if true,the count of the number of "." is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "a"</li>
   * <li>"/dir/a.b.c.d.e",1 → "a.b.c.d"</li>
   * <li>"/dir/a.b.c.d.e",2 → "a.b.c"</li>
   * <li>"/dir/a.b.c.d.e",4 → "a"</li>
   * <li>"/dir/a.b.c.d.e",5 → "a.b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",10 → "a.b.c.d.e"</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "a"</li>
   * <li>"/dir/a.b.c.d.e",1 → "a.b.c.d"</li>
   * <li>"/dir/a.b.c.d.e",2 → "a.b.c"</li>
   * <li>"/dir/a.b.c.d.e",4 → "a"</li>
   * <li>"/dir/a.b.c.d.e",5 → "a"</li>
   * <li>"/dir/a.b.c.d.e",10 → "a"</li>
   * </ul>
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getFileNameWithoutExtension(Path path,int dotCount,boolean fuzzy)throws NullPointerException{
    String name=getFileName(path);
    int index = indexOfExtension(path,dotCount,fuzzy);
    return index==-1?name:name.substring(0,index);
  }


  /**
   * get the file name without the extension.<br>
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "a"</li>
   * <li>"/dir/a.b.c.d.e",1 → "a.b.c.d"</li>
   * <li>"/dir/a.b.c.d.e",2 → "a.b.c"</li>
   * <li>"/dir/a.b.c.d.e",4 → "a"</li>
   * <li>"/dir/a.b.c.d.e",5 → "a.b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",10 → "a.b.c.d.e"</li>
   * </ul><br>
   * @param path non null.
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method removes the longest extension.
   * @return file extension
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static String getFileNameWithoutExtension(Path path,int dotCount)throws NullPointerException{
    return getFileNameWithoutExtension(path,dotCount,false);
  }

  /**
   * get the path without the extension.
   * <ul>
   * <li>"C:/a/test.txt" → "C:/a/test"</li>
   * <li>"abc.tar.gz" → "abc.tar"</li>
   * <li>"abc." → "abc"</li>
   * <li>"." → ""</li>
   * <li>".." → "."</li>
   * <li>"/"  → "/"</li>
   * <li>"C:/"  → "C:/"</li>
   * <li>""  → ""</li>
   * </ul>
   * @param path non null
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path removeExtension(Path path)throws NullPointerException{
    return (path.getNameCount()==0)?path:sibling(path,getFileNameWithoutExtension(path));
  }

  /**
   * get the path without the extension.
   * <ul>
   * <li>"/a/b/c.d.e.f" 0 → "/a/b/c"</li>
   * <li>"/a/b/c.d.e.f" 1 → "/a/b/c.d.e"</li>
   * <li>"/a/b/c.d.e.f" 2 → "/a/b/c.d"</li>
   * <li>"/a/b/c.d.e.f" 3 → "/a/b/c"</li>
   * <li>"/a/b/c.d.e.f" 4 → "/a/b/c.d.e.f"</li>
   * <li>"/" n → "/"</li>
   * <li>"C:/" n → "C:/"</li>
   * <li>"" n  → ""</li>
   * </ul>
   * @param path non null
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method removes the longest extension.
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @see #getExtension(Path, int, boolean)
   * @since 1.0.0
   */
  public static Path removeExtension(Path path,int dotCount)throws NullPointerException{
    return (path.getNameCount()==0)?path:sibling(path,getFileNameWithoutExtension(path,dotCount));
  }

  /**
   * get the path without the extension.
   * <ul>
   * <li>"/a/b/c.d.e.f" 0 → "/a/b/c"</li>
   * <li>"/a/b/c.d.e.f" 1 → "/a/b/c.d.e"</li>
   * <li>"/a/b/c.d.e.f" 2 → "/a/b/c.d"</li>
   * <li>"/a/b/c.d.e.f" 3 → "/a/b/c"</li>
   * <li>"/a/b/c.d.e.f" 4 → "/a/b/c.d.e.f"</li>
   * <li>"/" n → "/"</li>
   * <li>"C:/" n → "C:/"</li>
   * <li>"" n  → ""</li>
   * </ul>
   * @param path non null
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method removes the longest extension.
   * @param fuzzy if true,the count of the number of "." is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "/dir/a"</li>
   * <li>"/dir/a.b.c.d.e",1 → "/dir/a.b.c.d"</li>
   * <li>"/dir/a.b.c.d.e",2 → "/dir/a.b.c"</li>
   * <li>"/dir/a.b.c.d.e",4 → "/dir/a"</li>
   * <li>"/dir/a.b.c.d.e",5 → "/dir/a.b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",10 → "/dir/a.b.c.d.e"</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → "/dir/a"</li>
   * <li>"/dir/a.b.c.d.e",1 → "/dir/a.b.c.d"</li>
   * <li>"/dir/a.b.c.d.e",2 → "/dir/a.b.c"</li>
   * <li>"/dir/a.b.c.d.e",4 → "/dir/a"</li>
   * <li>"/dir/a.b.c.d.e",5 → "/dir/a"</li>
   * <li>"/dir/a.b.c.d.e",10 → "/dir/a"</li>
   * </ul>
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @see #getExtension(Path, int, boolean)
   * @since 1.0.0
   */
  public static Path removeExtension(Path path,int dotCount,boolean fuzzy)throws NullPointerException{
    return (path.getNameCount()==0)?path:sibling(path,getFileNameWithoutExtension(path,dotCount,fuzzy));
  }

  private static boolean isEmpty(String s){return s ==null || s.isEmpty();}

  /**
   * create a new path. "DIR/FILENAME.EXTENSION" → "DIR/<code>&lt;pre&gt;</code>FILENAME<code>&lt;insert&gt;</code>.<code>&lt;ext&gt;</code><code>&lt;post&gt;</code>"<br>
   * When the <code>ext</code> is null,"DIR/FILENAME.EXTENSION" → "DIR/<code>&lt;pre&gt;</code>FILENAME<code>&lt;insert&gt;</code>.EXTENSION<code>&lt;post&gt;</code>"<br>
   * When the <code>ext</code> is empty, "DIR/FILENAME.EXTENSION" → "DIR/<code>&lt;pre&gt;</code>FILENAME<code>&lt;insert&gt;</code><code>&lt;post&gt;</code>"<br>
   * <ul>
   * <li>"/NAME.TXT","pre_","_ins","org","_post",1 →"/pre_NAME_ins.org_post"</li>
   * <li>"NAME.TXT","pre_","_ins",<b><code>null</code></b>,"_post",1 →"pre_NAME_ins.TXT_post"</li>
   * <li>"NAME.TXT","pre_","_ins",<b>""</b>,"_post",1 →"pre_NAME_ins_post"</li>
   * <li>"/a/b.c.d.e.f","pre_","_ins","txt","_post",0 →"/a/pre_b_ins.txt_post"</li>
   * <li>"/a/b.c.d.e.f","pre_","_ins","txt","_post",2 →"/a/pre_b.c.d_ins.txt_post"</li>
   * <li>"/","pre_","_ins","txt","_post",0 →"/pre__ins.txt_post"</li>
   * <li>"C:/","pre_","_ins","txt","_post",0 →"C:/pre__ins.txt_post"</li>
   * </ul>
   * @param path non null.
   * @param pre  nullable
   * @param insert nullabel
   * @param ext  extension.If <code>ext</code> is <code>null</code>,the original extension of <code>path</code> is used.
   * @param post nullable.
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method uses the longest extension.
   * @param fuzzy if true,the count of the number of "." of the extension is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",1 → extension = "e"</li>
   * <li>"a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",5 → extension = ""</li>
   * <li>"a.b.c.d.e",10 → extension = ""</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",1 → extension = "e"</li>
   * <li>"a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",5 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",10 → extension = "b.c.d.e"</li>
   * </ul>
   * @return new path
   * @throws NullPointerException <code>path</code> is null
   * @since 1.0.0
   */
  public static Path newFileName(Path path,String pre,String insert,String ext,String post,int dotCount,boolean fuzzy)
      throws NullPointerException{
    requireNonNull(path);
    boolean ep = isEmpty(pre),ei=isEmpty(insert),ee=ext==null ,epo=isEmpty(post);
    String e = ee?"":(ext==null || ext.isEmpty())?"" : ext.charAt(0)=='.'?ext:'.'+ext;
    if(ep && ei && ee && epo){
      return path;
    }
    StringBuilder sb = new StringBuilder();
    if(!ep)sb.append(pre);
    if(path.getNameCount()==0){
      if(!ei)sb.append(insert);
      if(!ee)sb.append(e);
      if(!epo)sb.append(post);
      return resolve(path,sb.toString());
    }
    if(ee && ei){
      sb.append(path.getFileName().toString());
    }else{
      int index = indexOfExtension(path,dotCount,fuzzy);
      String fname = path.getFileName().toString();
      if(index == -1){
        sb.append(fname);
      }else if(index!=0){
        sb.append(fname,0,index);
      }
      if(!ei)sb.append(insert);
      if(!ee){
        sb.append(e);
      }else if(index != -1 && index!=fname.length()){
        sb.append(fname,index,fname.length());
      }
    }
    if(!epo)sb.append(post);
    return sibling(path,sb.toString());
  }

  /**
   * Returns the path that changed the extension of the file name of <code>path</code>.
   * If <code>path</code> has no extension,simply add the extension.
   * <ul>
   *  <li>"/test.txt","org" → "/test.org"</li>
   *  <li>"test.txt",".org" → "test.org"</li>
   *  <li>"/test",".org" → "/test.org"</li>
   *  <li>"/test.txt","" → "/test"</li>
   *  <li>"/test.txt",null → "/test"</li>
   *  <li>"/",".org" → "/.org"</li>
   *  <li>"","org" → ".org"</li>
   *  <li>".","org" → ".org"</li>
   * </ul>
   * @param path non null.
   * @param ext extension.(Either ".txt" or "txt" is possible.).nullable
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path replaceExtension(Path path,String ext)throws NullPointerException{
    return newFileName(path,null,null,ext==null?"":ext,null,1,false);
  }

  /**
   * Returns the path that changed the extension of the file name of <code>path</code>.
   * If <code>path</code> has no extension,simply add the extension.
   * <ul>
   *  <li>"/test.txt","org",1 → "/test.org"</li>
   *  <li>"test.tar.gz",".org",1 → "test.tar.org"</li>
   *  <li>"test.tar.gz",".org",2 → "test.org"</li>
   *  <li>"/a.b.c.d.e",".org",0 → "/a.org"</li>
   *  <li>"/test.txt","",0 → "/test"</li>
   *  <li>"/test.txt",null,0 → "/test"</li>
   *  <li>"/",".org",0 → "/.org"</li>
   *  <li>"","org",0 → ".org"</li>
   *  <li>".","org",0 → ".org"</li>
   * </ul>
   * @param path non null.
   * @param ext extension.(Either ".txt" or "txt" is possible.).nullable
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method uses the longest extension.
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path replaceExtension(Path path,String ext,int dotCount)throws NullPointerException{
    return newFileName(path,null,null,ext==null?"":ext,null,dotCount,false);
  }

  /**
   * Returns the path that changed the extension of the file name of <code>path</code>.
   * If <code>path</code> has no extension,simply add the extension.
   * <ul>
   *  <li>"/test.txt","org",1 → "/test.org"</li>
   *  <li>"test.tar.gz",".org",1 → "test.tar.org"</li>
   *  <li>"test.tar.gz",".org",2 → "test.org"</li>
   *  <li>"/a.b.c.d.e",".org",0 → "/a.org"</li>
   *  <li>"/test.txt","",0 → "/test"</li>
   *  <li>"/test.txt",null,0 → "/test"</li>
   *  <li>"/",".org",0 → "/.org"</li>
   *  <li>"","org",0 → ".org"</li>
   *  <li>".","org",0 → ".org"</li>
   * </ul>
   * @param path non null.
   * @param ext extension.(Either ".txt" or "txt" is possible.).nullable
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method uses the longest extension.
   * @param fuzzy if true,the count of the number of "." of the extension is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",1 → extension = "e"</li>
   * <li>"a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",5 → extension = ""</li>
   * <li>"a.b.c.d.e",10 → extension = ""</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",1 → extension = "e"</li>
   * <li>"a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",5 → extension = "b.c.d.e"</li>
   * <li>"a.b.c.d.e",10 → extension = "b.c.d.e"</li>
   * </ul>
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path replaceExtension(Path path,String ext,int dotCount,boolean fuzzy)throws NullPointerException{
    return newFileName(path,null,null,ext==null?"":ext,null,dotCount,fuzzy);
  }

  /**
   * add <code>str</code> to the file name of <code>path</code>.
   * <ul>
   *  <li>"a/b/c/test.txt","str_" → "a/b/c/str_test.txt"</li>
   *  <li>".","str_" → "str_."</li>
   *  <li>"/","str_" → "/str_"</li>
   *  <li>"/a/b/","str_" → "/a/str_b"</li>
   *  <li>"C:/","str_" → "C:/str_"</li>
   * </ul>
   * @param path non null.
   * @param str append string
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path prependFileName(Path path,String str)throws NullPointerException{
    return newFileName(path,str,null,null,null,1,false);
  }

  /**
   * add <code>str</code> to the file name of <code>path</code>.
   * <ul>
   *  <li>"a/b/c/test.txt","_str" → "a/b/c/test.txt_str"</li>
   *  <li>".","_str" → "._str"</li>
   *  <li>"/","_str" → "/_str"</li>
   *  <li>"/a/b/","_str" → "/a/b_str"</li>
   *  <li>"C:/","_str" → "C:/_str"</li>
   * </ul>
   * @param path non null.
   * @param str append string
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static Path appendFileName(Path path,String str){
    return newFileName(path,null,null,null,str,1,false);
  }

  /**
   * insert <code>str</code> before the extension of <code>path</code>.
   * <ul>
   *  <li>"a/b/c/test.txt","_str" → "a/b/c/test_str.txt"</li>
   *  <li>"a/b/c/test","_str" → "a/b/c/test_str"</li>
   *  <li>".","_str" → "_str."</li>
   *  <li>"/","_str" → "/_str"</li>
   *  <li>"/a/b/","_str" → "/a/b_str"</li>
   *  <li>"C:/","_str" → "C:/_str"</li>
   * </ul>
   * @param path path
   * @param str insert text
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method uses the longest extension.
   * @param fuzzy if true,the count of the number of "." of the extension is fuzzy.<br>
   * fuzzy = false:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",1 → extension = "e"</li>
   * <li>"/dir/a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"/dir/a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",5 → extension = ""</li>
   * <li>"/dir/a.b.c.d.e",10 → extension = ""</li>
   * </ul><br>
   * fuzzy = true:
   * <ul>
   * <li>"/dir/a.b.c.d.e",0 → extension = "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",1 → extension = "e"</li>
   * <li>"/dir/a.b.c.d.e",2 → extension = "d.e"</li>
   * <li>"/dir/a.b.c.d.e",4 → extension = "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",5 → extension = "b.c.d.e"</li>
   * <li>"/dir/a.b.c.d.e",10 → extension = "b.c.d.e"</li>
   * </ul>
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path insertFileName(Path path,String str,int dotCount,boolean fuzzy)
      throws NullPointerException{
    return newFileName(path,null,str,null,null,dotCount,fuzzy);
  }

  /**
   * insert <code>str</code> before the extension of <code>path</code>.
   * <ul>
   *  <li>"a/b/c/test.txt","_str" , 1 → "a/b/c/test_str.txt"</li>
   *  <li>"a/b/c/test.tar.gz","_str" , 2 → "a/b/c/test_str.tar.gz"</li>
   *  <li>"a/b/c/test.tar.gz","_str" , 3 → "a/b/c/test.tar.gz_str"</li>
   *  <li>"a/b/c/test.a.b.c.d.e","_str" , 0 → "a/b/c/test_str.a.b.c.d.e"</li>
   *  <li>"a/b/c/test","_str" , 1 → "a/b/c/test_str"</li>
   *  <li>".","_str" , 1 → "_str."</li>
   *  <li>"/","_str" , 1 → "/_str"</li>
   *  <li>"/a/b/","_str" , 1 → "/a/b_str"</li>
   *  <li>"C:/","_str" , 1 → "C:/_str"</li>
   * </ul>
   * @param path path
   * @param str insert text
   * @param dotCount the number of dots appear in the extension.
   * If <code>dotCount</code> is less than or equal to 0,this method uses the longest extension.
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path insertFileName(Path path,String str,int dotCount)
      throws NullPointerException{
    return insertFileName(path,str,dotCount,false);
  }

  /**
   * insert <code>str</code> before the extension of <code>path</code>.
   * <ul>
   *  <li>"a/b/c/test.txt","_str" → "a/b/c/test_str.txt"</li>
   *  <li>"a/b/c/test.tar.gz","_str" → "a/b/c/test.tar_str.gz"</li>
   *  <li>"a/b/c/test","_str" → "a/b/c/test_str"</li>
   *  <li>".","_str" → "_str."</li>
   *  <li>"/","_str" → "/_str"</li>
   *  <li>"/a/b/","_str" → "/a/b_str"</li>
   *  <li>"C:/","_str" → "C:/_str"</li>
   * </ul>
   * @param path path
   * @param str insert text
   * @return path
   * @throws NullPointerException <code>path</code> is <code>null</code>.
   * @since 1.0.0
   */
  public static Path insertFileName(Path path,String str)
      throws NullPointerException{
    return insertFileName(path,str,1,false);
  }

  /**
   * Open the file and create a new {@link BufferedReader}.
   * When <code>charset</code> is UTF-8 and the BOM is found,the reader will skip the BOM.
   * @param path file paht.non null.
   * @param charset if <code>charset</code> is <code>null</code>,{@link Charset#defaultCharset()} will be used.
   * @param options options
   * @return {@link BufferedReader}
   * @throws IOException if an I/O error occurs
   * @throws NullPointerException <code>path</code> is <code>null</code>
   * @since 1.0.0
   */
  public static BufferedReader newBufferedReader(Path path,Charset charset,OpenOption... options)
      throws IOException,NullPointerException{
    requireNonNull(path,"path is null");
    return newBufferedReader(Files.newInputStream(path,options!=null? options:new OpenOption[0]),charset);
  }

  /**
   * wrap <code>input</code> with a {@link BufferedReader}
   * @param inputStream input
   * @param charset if <code>charset</code> is <code>null</code>,{@link Charset#defaultCharset()} will be used.
   * @return {@link BufferedReader}
   * @throws IOException if an I/O error occurs
   * @throws NullPointerException <code>inputStream</code> is <code>null</code>
   * @since 1.0.0
   */
  public static BufferedReader newBufferedReader(InputStream inputStream,Charset charset)
      throws IOException,NullPointerException{
    if(charset==null)charset = Charset.defaultCharset();
    if(charset == UTF_8){
      if(!inputStream.markSupported()){
        inputStream = new BufferedInputStream(inputStream);
      }
      inputStream.mark(3);
      byte buf[] ={0,0,0};
      int read = inputStream.read(buf,0,3);
      boolean bom=false;
      if(read == 3){
        bom = buf[0] == (byte)0xEF && buf[1] == (byte)0xBB && buf[2] == (byte)0xBF;
      }
      if(!bom){
        inputStream.reset();
      }
    }
    return new BufferedReader(new InputStreamReader(inputStream,charset));
  }

  /**
   * called from {@link FileVisitor#visitFile(Object, BasicFileAttributes)}
   * or {@link FileVisitor#preVisitDirectory(Object, BasicFileAttributes)}.
   * @author nodamushi
   * @since 1.0.0
   */
  @FunctionalInterface public static interface Visit{
    /**
     * @see FileVisitor#visitFile(Object, BasicFileAttributes)
     * @see FileVisitor#preVisitDirectory(Object, BasicFileAttributes)
     * @param path path
     * @param attrs attributes
     * @return FileVisitResult. DON'T return <code>null</code>.
     * @throws IOException exception
     */
    public FileVisitResult visit(Path path,BasicFileAttributes attrs)
        throws IOException;
  }

  private static class F implements FileVisitor<Path>{
    private final Visit v;
    private final boolean d;
    private final int m;
    private int t;

    private F(Visit visit,boolean directoryMode,int directoryMaxDepth){
      v=requireNonNull(visit);
      d=directoryMode;
      m=directoryMaxDepth;
      t = -1;
    }

    @Override public FileVisitResult preVisitDirectory(Path dir,
        BasicFileAttributes attrs) throws IOException{
      FileVisitResult ret=d?v.visit(dir,attrs):FileVisitResult.CONTINUE;
      if(m < 0){
        return FileVisitResult.CONTINUE;
      }
      if(t!=m){
        if(ret==FileVisitResult.CONTINUE){
          t++;
        }
        return ret;
      }else{
        return FileVisitResult.SKIP_SUBTREE;
      }
    }

    @Override public FileVisitResult visitFile(Path file,BasicFileAttributes attrs)
        throws IOException{
      return d?FileVisitResult.CONTINUE:v.visit(file,attrs);
    }

    @Override public FileVisitResult visitFileFailed(Path file,IOException exc)
        throws IOException{
      throw exc;
    }

    @Override public FileVisitResult postVisitDirectory(Path dir,IOException exc)
        throws IOException{
      if(0 <= m){
        t--;
      }
      if(exc!=null) throw exc;
      return FileVisitResult.CONTINUE;
    }
  }
  /**
   * only implements {@link FileVisitor#visitFile(Object, BasicFileAttributes)},and call {@link Files#walkFileTree(Path, FileVisitor)}.<br>
   * @param start start path
   * @param directoryMaxDepth Max depth of the directory access.If <code>directoryMaxDepth</code> is less than 0, it is considered to be unlimited.
   * @param visitFile called from {@link FileVisitor#visitFile(Object, BasicFileAttributes)}
   * @throws IOException I/O exception occur.
   * @throws NullPointerException <code>path , visitFile</code> are null
   * @since 1.0.0
   */
  public static void walkFiles(Path start,int directoryMaxDepth,Visit visitFile)
      throws IOException{
    Files.walkFileTree(start,new F(visitFile,false,directoryMaxDepth));
  }
  /**
   * only implements {@link FileVisitor#preVisitDirectory(Object, BasicFileAttributes)},and call {@link Files#walkFileTree(Path, FileVisitor)}.<br>
   * @param start start path
   * @param directoryMaxDepth Max depth of the directory access.If <code>directoryMaxDepth</code> is less than 0, it is considered to be unlimited.
   * @param visitDirectory called from {@link FileVisitor#preVisitDirectory(Object, BasicFileAttributes)}
   * @throws IOException I/O exception occur.
   * @throws NullPointerException <code>path , visitDirectory</code> are null
   * @since 1.0.0
   */
  public static void walkDirectories(Path start,int directoryMaxDepth,Visit visitDirectory)
      throws NullPointerException, IOException{
    Files.walkFileTree(start,new F(visitDirectory,true,directoryMaxDepth));
  }

  /**
   * Path Iterator Option.
   * @author nodamushi
   * @since 1.0.0
   */
  public static enum ItrOption{
    /**
     * C:/d/e.txt
     * <ul><li> "C:/"</li><li> "d"</li> <li> "e.txt" </li></ul>
     * /c/d/e.txt
     * <ul><li> "/"</li><li>"c"</li><li> "d"</li> <li> "e.txt" </li></ul>
     * c/d/e.txt
     * <ul><li>"c"</li><li> "d"</li> <li> "e.txt" </li></ul>
     * @since 1.0.0
     */
    NAME_ONLY,
    /**
     * C:/d/e.txt
     * <ul><li> "C:/"</li><li> "C:/d"</li><li> "C:/d/e.txt" </li></ul>
     * /c/d/e.txt
     * <ul><li> "/"</li><li>/c</li><li> "/c/d"</li><li> "/c/d/e.txt" </li></ul>
     * c/d/e.txt
     * <ul><li>"c"</li><li> "c/d"</li><li> "c/d/e.txt" </li></ul>
     * @since 1.0.0
     */
    FULL_PATH,
    /**
     * C:/d/e.txt
     * <ul>
     *  <li> "C:/" (if c drive exists)</li>
     *  <li> "C:/d" (if "C:/d" exists) </li>
     *  <li> "C:/d/e.txt" (if C:/d/e.txt" exists)</li>
     * </ul>
     * /c/d/e.txt
     * <ul>
     *  <li> "/" (if root "/" is exist)</li>
     *  <li>/c</li>
     *  <li>"/c/d" (if "/c/d" is exist)</li>
     *  <li>"/c/d/e.txt" (if "/c/d/e.txt" is exist)</li>
     * </ul>
     *
     * c/d/e.txt
     * <ul>
     * <li>"c" (if "./c" is exist)</li>
     * <li> "c/d" (if "./c/d" is exist)</li>
     * <li> "c/d/e.txt" (if "./c/d/e.txt" exist)</li>
     * </ul>
     * @since 1.0.0
     */
    EXIST_ONLY,
  }
  /**
   * create path {@link Iterable}
   * @param path path.if <code>path</code> is null,this function return empty Iterable(not return <code>null</code>).
   * @param startIndex first index of iterator.(* greater than or equal to 0.)
   * @param option iterator type.if <code>option</code> is <code>null</code>, {@link ItrOption#FULL_PATH} is used.
   * @return {@link Iterable}
   * @throws IllegalArgumentException startIndex &lt; 0
   * @since 1.0.0
   */
  public static Iterable<Path> iterator(Path path,int startIndex,ItrOption option)
      throws IllegalArgumentException{
    if(startIndex < 0){
      throw new IllegalArgumentException(
          format("startIndex < 0.  :%d",startIndex));
    }
    if(option == null){
      option = ItrOption.FULL_PATH;
    }
    ItrOption o = option;
    return ()->new PItr(path,
        o!=ItrOption.NAME_ONLY,
        o==ItrOption.EXIST_ONLY,startIndex);
  }

  /**
   * create path {@link Iterable}
   * @param path path.if <code>path</code> is <code>null</code>,this function return empty Iterable(not return <code>null</code>).
   * @param option iterator type.if <code>option</code> is <code>null</code>, {@link ItrOption#FULL_PATH} is used.
   * @return {@link Iterable}
   * @since 1.0.0
   */
  public static Iterable<Path> iterator(Path path,ItrOption option){
    return iterator(path,0,option);
  }

  /**
   * create path {@link Iterable}.
   * <code>(itarator(path,0,null))</code>
   * @param path path.if path is <code>null</code>,this function return empty Iterable(not return <code>null</code>).
   * @return {@link Iterable}
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static Iterable<Path> iterator(Path path){
    return iterator(path,0,null);
  }

  /**
   * create stream from {@link #iterator(Path, int, ItrOption)}.
   * @param path path.nullable
   * @param startIndex start index of path
   * @param option option.nullable (default is {@link ItrOption#FULL_PATH})
   * @return {@link Stream}
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static Stream<Path> stream(Path path,int startIndex,ItrOption option){
    if(option == null){
      option = ItrOption.FULL_PATH;
    }
    PItr i = new PItr(path,option!=ItrOption.NAME_ONLY,
        option==ItrOption.EXIST_ONLY,startIndex);
    int length = i.size-i.index;
    return StreamSupport.stream(Spliterators.spliterator(i,length,
        Spliterator.IMMUTABLE |Spliterator.NONNULL|Spliterator.SIZED|
        (option==ItrOption.NAME_ONLY?0:Spliterator.DISTINCT)
        ),false);
  }

  /**
   * create stream from {@link #iterator(Path, ItrOption)}
   * @param path path.nullable
   * @param option option.nullable (default is {@link ItrOption#FULL_PATH})
   * @return {@link Stream}
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static Stream<Path> stream(Path path,ItrOption option){
    return stream(path,0,option);
  }

  /**
   * create stream from {@link #iterator(Path)}
   * @param path path
   * @return {@link Stream}
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static Stream<Path> stream(Path path){
    return stream(path,0,null);
  }

  /**
   * <code>for(Path p:itarator(path,startIndex,option)) func.accept(p);</code>
   * @param path path
   * @param startIndex startIndex
   * @param option option.
   * @param func called function for each path.if <code>func</code> is <code>null</code>, this method do nothing.
   * @throws IllegalArgumentException startIndex &lt; 0.
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static void forEach(Path path,int startIndex,ItrOption option,Consumer<Path> func)
      throws IllegalArgumentException{
    if(path == null || func == null){
      return;
    }
    for(Path p:iterator(path,startIndex,option)){
      func.accept(p);
    }
  }

  /**
   * <code>for(Path p:itarator(path,0,option)) func.accept(p);</code>
   * @param path path
   * @param option option.
   * @param func called function for each path.if <code>func</code> is <code>null</code>, this method do nothing.
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static void forEach(Path path,ItrOption option,Consumer<Path> func){
    forEach(path,0,option,func);
  }

  /**
   * for(Path p:itarator(path,0,FULL_PATH)) func.accept(p);
   * @param path path
   * @param func called function for each path.if <code>func</code> is <code>null</code>, this method do nothing.
   * @see #iterator(Path, int, ItrOption)
   * @since 1.0.0
   */
  public static void forEach(Path path,Consumer<Path> func){
    forEach(path,0,null,func);
  }

  private static class PItr implements Iterator<Path>{
    private final Path path;
    private final int size;
    private final boolean fullPath;
    private final boolean hasRoot;
    private int index;

    private PItr(Path path,boolean fullPath,boolean existCheck,int start){
      this.path = path;
      this.fullPath = fullPath;
      if(path == null){
        hasRoot = false;
        size  =0;
      }else{
        hasRoot = path.getRoot()!=null;
        int maxsize = path.getNameCount() + (hasRoot ? 1:0);
        if(existCheck){
          for(int i=0;i!=maxsize;i++){
            Path p=subpath(i);
            if(!Files.exists(p)){
              maxsize = i;
              break;
            }
          }
        }
        size = maxsize;
      }
      if(size < start){
        index = size;
      }else{
        index = start;
      }
    }

    private Path subpath(int index){
      if(hasRoot){
        return index==0?path.getRoot():path.getRoot().resolve(path.subpath(0,index));
      }else{
        return path.subpath(0,index+1);
      }
    }

    private Path getName(int index){
      if(hasRoot){
        return index==0?path.getRoot():path.getName(index-1);
      }else{
        return path.getName(index);
      }
    }

    @Override public boolean hasNext(){
      return size != index;
    }

    @Override public Path next(){
      Path p = fullPath? subpath(index):getName(index);
      index++;
      return p;
    }
  }

  private NPaths(){}
}
