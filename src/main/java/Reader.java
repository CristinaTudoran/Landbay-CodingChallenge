import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public record Reader(String filePath) {

    //constructor
    private List<String> getRecordFromLine(String line) {

        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    //read data line by line and store it in a list
    private List<List<String>> readData() {

        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (Exception e) {
            System.out.println("Error: file cannot be read");
        }

        return records;
    }

    //check if any elements were split in two because of a comma; it works only when there is 1 comma
    //when there are two or more commas, only the first 2 parts are recorded in the HashMap
    private List<List<String>> checkData(List<List<String>> records, int size){

        for (List<String> record : records) {
            if (record.size() > size) {
                int delIndex = -1;
                int replaceIndex = -1;
                String firstHalf = null;
                String secondHalf = null;
                for (String element : record) {
                    if (element.startsWith("\"")) {
                        firstHalf = element.replace("\"", "");
                        replaceIndex = record.indexOf(element);
                    } else if (element.endsWith("\"")) {
                        secondHalf = element.replace("\"", "");
                        delIndex = record.indexOf(element);
                    }
                }
                if (delIndex + replaceIndex != -2) {
                    String newElement = firstHalf + secondHalf;
                    record.remove(delIndex);
                    record.set(replaceIndex, newElement);
                }
            }
        }
        return records;
    }

    //parse the records from the file in a HashMap
    public HashMap<String, List<String>> getData() {

        List<List<String>> oldRecords = readData();
        //get the size of the records in the file i.e. the number of attributes they have
        int size = oldRecords.get(0).size();

        //get the data after checking the input format is correct
        List<List<String>> records = checkData(oldRecords, size);

        //create the HashMap object
        HashMap<String, List<String>> map = new HashMap<>();

        int iterations = 0;

        for (List<String> record : records) {
            if (iterations == 0) {
                iterations++;
                continue;
            }
            //construct two new lists, the first which is the key of the HashMap and the other being the value
            ArrayList<String> key = new ArrayList<>(record.subList(0, 1));
            List<String> value = new ArrayList<>(record.subList(1, size));

            String actualKey = key.get(0);

            //check if key already is in the HashMap
            if (map.containsKey(actualKey)) {
                //if yes, add the new item to the existing list of elements belonging to this key
                List<String> oldValue = map.get(actualKey);
                List<String> newValue = new ArrayList<>();
                newValue.addAll(oldValue);
                newValue.addAll(value);
                map.put(actualKey, newValue);
            } else {
                //add record to the hashMap
                map.put(actualKey, value);
            }
        }
        return map;
    }
}