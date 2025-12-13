import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    Schedule schedules[];
    private static String clean(String text) {
        if (text == null) return "";
        return text.replace("\uFEFF", "").trim();
    }
    public static List<String[]>  FileReader(String filePath){
        List<String[]> rows = new ArrayList<>();
        if(filePath==null || filePath.trim().isEmpty() || !Files.exists(Path.of(filePath))){
            return rows;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line =br.readLine() ) != null){
                line =clean(line);
                if (line.isEmpty()) {

                    continue;
                }
                rows.add(line.split(","));
            }

        } catch (IOException e){
            System.out.println("File read error: " + e.getMessage());
        }
        return  rows;
    }

    public static void FileWriter(){

    }

    public static void ExportSchedule(Schedule[] schedules){

    }
}
