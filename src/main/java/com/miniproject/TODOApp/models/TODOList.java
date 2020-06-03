package com.miniproject.TODOApp.models;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import com.opencsv.CSVReader; 
import com.opencsv.CSVWriter;


public class TODOList {
    private static final String CSV_PATH = "./TODOList.csv";
    // private static ArrayList<TODOList> todoLists = getTodoLists();
    // private static ArrayList<TODOList> todoListsToday = getTodoListsToday();
    private String name;
    private String description;
    private String dueDate;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        // capitalize the first letter of the task name
        String cases = name.substring(0,1).toUpperCase();
        this.name = cases + name.substring(1);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDueDate() {
        return this.dueDate;
    }

    public String parseDueDate(String date) {
        // buat dipake di templates, biar tanggalnya lebih human readable
        // contoh: input user 2020-04-04, di viewnya April 4, 2020
        LocalDate tgl = LocalDate.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, uuuu");
        String date2 = tgl.format(formatter);
        return date2;
    }

    public void setDueDate(String date) {
        this.dueDate = date;
    }

    public static ArrayList<TODOList> getTodoLists() {
        // agar todoLists ter-update tiap di-get
        ArrayList<TODOList> listTodo = new ArrayList<>();
        ArrayList<TODOList> newListTodo = new ArrayList<>();

        // adding the data from csv to listTodo
        // agar listTodo dan newListTodo ter-update tiap di-get
        try (
            Reader reader = Files.newBufferedReader(Paths.get(CSV_PATH));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {
            for (CSVRecord csvRecord : csvParser) {
                TODOList todo = new TODOList();

                todo.name = csvRecord.get(0);
                todo.dueDate = csvRecord.get(1);
                todo.description = csvRecord.get(2);

                listTodo.add(todo);
            }
            csvParser.close();
        } catch (Exception e){}

        for (TODOList todo : listTodo){
            if (!hasPassed(todo.getDueDate())){
                newListTodo.add(todo);
            } // kalau tgl yang diisi lewat dari hari ini, auto gak ke-add
        }
      
        Collections.sort(newListTodo, new Comparator<TODOList>() {
            // nge-sort berdasarkan dueDate
            @Override
            public int compare(TODOList o1, TODOList o2) {
                int value1 = LocalDate.parse(o1.getDueDate()).compareTo(LocalDate.parse(o2.getDueDate()));
                if (value1 == 0) {
                    // kalau dueDate sama, di-sort berdasarkan name
                    return o1.getName().compareTo(o2.getName());
                }
                return value1;
            }
        });

        return newListTodo;
    }

    public static ArrayList<TODOList> getTodoListsToday() {
        ArrayList<TODOList> newListsToday = new ArrayList<>();

        // agar todoListsToday ter-update tiap di-get
        for (TODOList todo : getTodoLists()){
            if (todo.isDueToday()){
                newListsToday.add(todo);
            }
        }
      
        Collections.sort(newListsToday, new Comparator<TODOList>() {
            // nge-sort berdasarkan name
            @Override
            public int compare(TODOList o1, TODOList o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return newListsToday;
    }

    public static void addTodo(TODOList todo){
        // write the data to TODOList.csv so the data wont disappear
        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_PATH), StandardCharsets.UTF_8, 
                StandardOpenOption.APPEND);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        ) {
            csvPrinter.printRecord(todo.getName(), todo.getDueDate(), todo.getDescription());
            csvPrinter.flush();   
            csvPrinter.close();      
        } catch (Exception e){}
    }

    public static void deleteTodo(TODOList task) {
        // delete a todo from TODOList.csv
        int num = 0;
        try ( CSVReader reader2 = new CSVReader(new FileReader(CSV_PATH));){
            List<String[]> allElements = reader2.readAll();   
            for (String[] todo : allElements) {
                if (todo[0].equals(task.getName())) break;
                else num++;
            }
            allElements.remove(num);
            FileWriter sw = new FileWriter(CSV_PATH);
            CSVWriter writer = new CSVWriter(sw);
            writer.writeAll(allElements);
            writer.close();
        } catch (Exception e) {}
    }

    public Boolean isDueToday() {
        // a method to check whether the todo dueDate is due today or not
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        String dateNow = date.format(formatter);

        if (dueDate.equals(dateNow)) {
            return true;
        }

        return false;
    } 

    public static Boolean hasPassed(String other) {
        // a method to check whether the dueDate has passed today or not
        LocalDate date = LocalDate.now();
        if (date.compareTo(LocalDate.parse(other)) > 0) {
            return true;
        }
        return false;
    }

    public static TODOList[] getNearestDeadlines() {
        // a method to get the nearest deadlines
        // to implement nearest deadlines view on index.html
        if (getTodoLists().size() != 0) {
            if (getTodoLists().size() == 1) {
                TODOList[] nearestDeadlines = new TODOList[1];
                nearestDeadlines[0] = getTodoLists().get(0);
                return nearestDeadlines;
            }
            else if (getTodoLists().size() == 2) {
                TODOList[] nearestDeadlines = new TODOList[2];
                for(int i = 0; i < 2; i++) {
                    nearestDeadlines[i] = getTodoLists().get(i);
                }
                return nearestDeadlines;
            }
            else if (getTodoLists().size() >= 3) {
                TODOList[] nearestDeadlines = new TODOList[3];
                for(int i = 0; i < 3; i++) {
                    nearestDeadlines[i] = getTodoLists().get(i);
                }
                return nearestDeadlines;
            }
        }
        TODOList[] nearestDeadlines = {};
        return nearestDeadlines;
    }

    public static String randomQuote() throws FileNotFoundException, IOException {
        // a method to implement random quote view on index.html
        
        // File quotes = new File("./src/main/resources/static/text/quotes.txt");
        // Scanner sc = new Scanner(quotes);
        Random random = new Random();
        
        List<String> listQuotes = Files.readAllLines(Paths.get("./src/main/resources/static/text/quotes.txt"));
        String quote = listQuotes.get(random.nextInt(listQuotes.size()));

        // for (int i = 0; i < randomLine; i++) {
        //     quote = sc.nextLine();
        // }

        // sc.close();
        return quote;
    }
    
}