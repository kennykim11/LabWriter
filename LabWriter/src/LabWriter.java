/**
 * LabWriter.java
 * @author Kenny Kim (kek9689@rit.edu)
 * @version 1.0
 * @since 9/20/18
 *
 * Have you ever had homework that had to follow Eclipse-generated HTML javadocs?
 * The fields and headers of the methods on the docs are written exactly as they are in code and the included docs
 *  are ideal.
 * But now you have to waste so much time referring back and forth to the docs just to make sure everything is exact.
 * I wish there was a program to parse the HTML javadocs and write the easy part of the code for me...
 */

// === IMPORTS ===

import java.io.*;
import java.util.Scanner;

import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class LabWriter {

    // === FIELDS ====

    static boolean ASK_FOR_FOLDER_LOCATION = false;
    static boolean ASK_FOR_PROJECT_NAME = true;
    static boolean ASK_FOR_OVERWRITE = true;
    static boolean ASK_FOR_WEBSOURCE = true;
    //author goes after class documentation, one empty line, and the name of the class
    static String authorInfo =
            ".java\n" +
            "@author Kenny Kim (kek9689@rit.edu)\n" +
            "@version 1.0\n" +
            "@since ";


    // ===METHODS ===

    // =Debugging=
    /**
     * childrenNames: This method returns a string of all of its children's tag and class
     * @param element The element whose children should be logged
     * @return String One line of all the element's children's tag and if applicable, class, in format "tag:class, "
     */
    public static String childrenNames(Element element){
        String concl = "";
        for (Element child : element.children()){
            concl += child.tagName();
            if (!child.className().equals("")) concl += ":" + child.className();
            concl += " ";
        }
        return concl;
    }

    /**
     * printInfo: This method prints basic short info about the given element
     * @param element Element to print info about
     */
    public static void printInfo(Element element){
        System.out.println("ELEMENT: " + element.id());
        System.out.println(" Class Name: " + element.className());
        System.out.println(" Tag Name: " + element.tagName());
        System.out.println(" Text: " + element.text());
        //System.out.println(" H3: " + element.getElementsByTag("h3").first().text());
        System.out.println(" # of Children: " + element.children().size());
        System.out.println(" Children: " + childrenNames(element));
    }

    /**
     * printInfo: This method prints to printInfo of each Element in the Elements
     * @param elements Elements to print info of
     */
    public static void printInfo(Elements elements){
        for (Element element : elements){
            printInfo(element);
        }
    }


    // =Casting to Comment=
    /**
     * comment: This is the default comment method, which takes in a string and comments it with minimum 110
     * characters per line
     * @param string String to comment
     * @param docs Whether to start with "/**" or "/*"
     */
    public static String comment(String string, boolean docs){
        return comment(string, docs, 110);
    }

    /**
     * comment: This is the default comment method, which takes in a string and comments it with a given minimum
     * characters per line
     * @param string String to comment
     * @param docs Whether to start with "/**" or "/*"
     * @param charCountPerLine How many chars until putting next word in a new line
     */
    public static String comment(String string, boolean docs, int charCountPerLine){
        //purposefully rounding down
        String concl = "";
        String[] words = string.split(" ");
        int counter = 0;
        String tempString = "";
        while (counter < words.length){
            if (words[counter].length() + tempString.length() < charCountPerLine){
                tempString += words[counter] + " ";
                if (words[counter].contains("\n")) {//in case the word has \n in it
                    concl += tempString;
                    tempString = "";
                }
                counter++;
            }
            else{
                //if a word is longer than charCount, make it a line by itself
                if (tempString.length() == 0){
                    concl += words[counter] + " \n";
                    counter++;
                }
                ///most other cases are this
                else{
                    concl += tempString + "\n";
                    tempString = "";
                }
            }
        }
        return comment(concl + tempString, docs, "\n");
    }

    /**
     * comment: This is the default comment method, which takes in a string and comments it, separating lines by a
     * string delimiter
     * @param string String to comment
     * @param docs Whether to start with "/**" or "/*"
     * @param delimiter Where to split the string
     */
    public static String comment(String string, boolean docs, String delimiter){
        String concl = docs ? "/**\n" : "/*\n";
        for (String line : string.split(delimiter)){
            concl += " * " + line + "\n";
        }
        return concl + " */";
    }

    /**
     * tab: Inserts a number of tabs to the beginning of every line
     * @param string String to comment
     * @param number How many tabs to insert
     * @return String New String with tabs in front
     */
    public static String tab(String string, int number){
        String concl = "";
        for (String line : string.split("\n")){
            for (int i = 0; i < number; i++){
                concl += "    ";
            }
            concl += line + "\n";
        }
        return concl;
    }




    // =Writing to File=
    /**
     * writeField: This method parses a field and writes the documentation and header to the file
     * @param element Element, ideally a dt or dd tag
     * @param writer The PrintWriter used to write to the file
     */
    public static int writeField(Element element, PrintWriter writer){
        try {
            writer.print(tab(comment(element.select("div.block").first().text(), true),1));
        }
        catch (NullPointerException e){return 1;}
        writer.println(tab(getPreText(element) + ";\n", 1)); //public int FIELDNAME;
        return 0;
    }

    /**
     * writeMethod: This method parses a field and writes the documentation and header to the file
     * @param element Element, ideally a dt or dd tag
     * @param writer The PrintWriter used to write to the file
     */
    public static int writeMethod(Element element, PrintWriter writer){
        String tag = "";
        String paramreturn = "";
        String text = "";
        try {
            String docs = element.select("div.block").first().text();
            Elements dt_dd = element.getElementsByTag("dl").first().children();
            docs += dt_dd.size() > 1 ? "\n" : "";
            for (int i = 0; i < dt_dd.size(); i++) { //skipping the first because that is the word "
                text = dt_dd.get(i).text();
                if (text.equals("Parameters:")) {
                    tag = "@param ";
                    continue;
                }
                if (text.equals("Returns:")) {
                    tag = "@return ";
                    continue;
                }
                if (text.equals("Throws:")) {
                    tag = "@throws ";
                    continue;
                }
                if (tag.equals("")) continue;
                paramreturn += " \n" + tag + dt_dd.get(i).text().replace(" - ", " ");
            }
            writer.print(tab(comment(docs + paramreturn, true),1));
        }
        catch (java.lang.NullPointerException e){}
        String header = getPreText(element);
        String body = header.contains("abstract") ? ";\n" : " {\n    \n}\n"; //end in semicolon if abstract
        writer.println(tab(header + body,1)); //public int FUNCNAME {
        return 0;
    }

    /**
     * writeBlocks: This method takes in a blockList, determines if it is a field or constructor/method
     * and forwards it to the appropriate method. Will add up all the methods status returns to see if successful (0)
     * @param element Ideally a blockList element
     * @param writer The PrintWriter used to write to the file
     */
    public static void writeBlocks(Element element, PrintWriter writer){
        int concl = 0;
        String type = element.getElementsByTag("h3").first().text().split(" ")[0];
        System.out.print(" Parsing " + type + "s: ");
        for (Element block: element.select("ul.blockList,ul.blockListLast > li.blockList")){
            if (type.equals("Field")) concl += writeField(block, writer);
            else concl += writeMethod(block, writer);
        }
        if (concl == 0) System.out.println("Success");
        else System.out.println("Failure");
    }


    // =Parsing=
    /**
     * getPreText: The text in a pre tag gets weird. This makes it like real sentences.
     * @param element The parent of the pre tag
     * @return String The formatted string.
     */
    public static String getPreText(Element element){
        return element.getElementsByTag("pre").first().wholeText().replaceAll("\n", " ").replaceAll(" +", " ").replaceAll("\u00A0", " ");
    }


    // === MAIN ===

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        try {
            // =SETUP=
            char delimiter = System.getProperty("os.name").contains("Windows") ? '\\' : '/';
            String folderLoc = "/Users/kenny/Desktop/Programming/Java/CS2/";
            //folder location
            if (ASK_FOR_FOLDER_LOCATION){
                System.out.println("Insert projects location:");
                folderLoc = scanner.next();
                if (!"/".equals(folderLoc.charAt(folderLoc.length()-1))) folderLoc += delimiter;
            }
            //project name
            String projectName = "Test";
            if (ASK_FOR_PROJECT_NAME){
                System.out.println("Type project name:");
                projectName = scanner.next();
            }
            //overwrite
            boolean overwrite = false;
            if (!(new File(folderLoc + projectName + delimiter + "src")).mkdirs()) {
                System.out.println("Project with name " + projectName + " at location \"" + folderLoc + "\" already exists");
                if (ASK_FOR_OVERWRITE) {
                    while (true) {
                        System.out.println("Overwrite existing files? (y/n)");
                        String ans = scanner.next();
                        if (ans.toLowerCase().equals("n")){
                            System.out.println("Not overwriting");
                            break;
                        }
                        else if (ans.toLowerCase().equals("y")) {
                            overwrite = true;
                            System.out.println("Overwriting");
                            break;
                        }
                    }
                }
            }
            //url
            String location = folderLoc + projectName + delimiter;
            String url = "https://www.cs.rit.edu/~csci142/Labs/04/Doc/";
            Elements links = new Elements();
            while (true){
                if (ASK_FOR_WEBSOURCE){
                    System.out.println("Type URL to scrape from (please end in '/'):");
                    url = scanner.next();
                }
                try {
                    links = Jsoup.connect(url).get().select("a[href]");
                    break;
                }
                catch (java.lang.IllegalArgumentException e){
                    System.out.println("Not a real URL, enter again\n");
                }
            }



            //downloading list of all the pages
            System.out.print("\nDonwloading List HTML: ");
            links = Jsoup.connect(url + "allclasses.html").get().select("a[href]");
            System.out.println("Success");



            // =Page Loop=
            for (Element element: links) {
                String link = element.attr("href");
                String className = element.text(); //aka Appliance
                String fileLoc = location + "src" + delimiter + className + ".java";
                if (new File(fileLoc).exists() && !overwrite) continue;
                System.out.println(fileLoc); //aka /Users/kenny/Desktop/Programming/Java/CS2/test/Appliance.java
                PrintWriter writer = new PrintWriter(fileLoc, "UTF-8");
                Document page = Jsoup.connect(url + link).get();
                Element contentContainer = page.getElementsByAttributeValue("class", "contentContainer").first();

                // =Documentation=
                System.out.print(" Parsing Class Documentation: ");
                try {
                    writer.println(comment(contentContainer.select("div.description > ul.blockList > li.blockList > div.block").first().wholeText().replaceAll("\n", "") + " \n\n" + className + authorInfo + java.time.LocalDate.now(), true) + "\n\n");
                    System.out.println("Success");
                }
                catch (java.lang.NullPointerException e) {System.out.println("Failure");}

                // =Class Header=
                //public class CLASS extends SUPER
                System.out.print(" Parsing Class Header: ");
                try {
                    writer.println(getPreText(contentContainer.select("div.description > ul.blockList > li.blockList").first()) + "{\n");
                    System.out.println(" Success");
                }
                catch (java.lang.NullPointerException e) {System.out.println(" Failure");}

                // =Fields, Constructors, Methods=
                for (Element block : page.select("div.details > ul.blockList > li.blockList > ul.blockList > li.blockList")){
                    writeBlocks(block, writer);
                }

                System.out.println();
                writer.println("\n}");
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("We got an error");
            e.printStackTrace();
        }
        System.out.println("FINISHED");
    }
}
