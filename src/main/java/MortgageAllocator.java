import java.util.*;
import java.text.DecimalFormat;

public class MortgageAllocator {

    public void mortgageAllocator(){
        allocateMortgages();
    }

    //get the location of the files where the data for the mortgage allocation is located
    private String getFilePath(String fileName){
        //get the file URL
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).toExternalForm();
        //if the file path starts with "file:/", eliminate it
        if (filePath == null) {
            throw new IllegalArgumentException("file not found!");
        }
        if(filePath.startsWith("file:/")) {
            filePath = filePath.substring(6);
        }
        else if (filePath.startsWith("jar:file:/")){
            filePath = filePath.substring(10);
        }
        else if (filePath.contains("!")){
            filePath = filePath.replace("!", "");
        }

        return filePath;
    }

    private static final DecimalFormat df = new DecimalFormat("0.00");

    //the method below returns a HashMap with the number of funds willing to fund each product
    private HashMap<String, Integer> getProdFunderNo(HashMap<String, List<String>> funderRecords){
        HashMap<String, Integer> prodFunders = new HashMap<>();

        for (Map.Entry<String, List<String>> fund : funderRecords.entrySet()) {
            List<String> productOptions = fund.getValue();
            for (String prod : productOptions){
                //check if key already is in the HashMap
                if(prodFunders.containsKey(prod)){
                    //if yes, increase the number of funders willing to fund this specific product
                    prodFunders.replace(prod, prodFunders.get(prod) + 1);
                }
                else{
                    prodFunders.put(prod, 1);
                }
            }
        }
        return prodFunders;
    }

    //the method below returns a HashMap with the loan amount each products has available for investment
    private HashMap<String, Integer> getProdLoanAmount(HashMap<String, List<String>> mortgageRecords){
        HashMap<String, Integer> productLoan = new HashMap<>();
        for(Map.Entry<String, List<String>> loan : mortgageRecords.entrySet()){
            List<String> loanDetails = loan.getValue();
            String potentialKey = loanDetails.get(1);
            //System.out.println(potentialKey);
            //check if key already is in the HashMap
            if(productLoan.containsKey(potentialKey)){
                //if yes, change the amount of money available for funding for that product
                Integer oldValue = productLoan.get(potentialKey);
                Integer newValue = oldValue + Integer.parseInt(loanDetails.get(0));
                productLoan.put(potentialKey, newValue);
            }
            else{
                productLoan.put(potentialKey, Integer.parseInt(loanDetails.get(0)));
            }
        }
        return productLoan;
    }

    //the method below calculates the total amount available which needs funding
    private float getTotalLoanValue(HashMap<String, List<String>> mortgageRecords){
        float totalLoanVal = 0;
        for(Map.Entry<String, List<String>> item: mortgageRecords.entrySet()){
            List<String> value = item.getValue();
            totalLoanVal += Integer.parseInt(value.get(0)); //0 is the loan amount index in the mortgage attribute list
        }
        return totalLoanVal;
    }

    //the method below reads the csv files needed for the mortgage allocation and allocates the given mortgages to
    //their potential funders
    public void allocateMortgages() {

        Display view = new CmdLineInterface();

        String startMortgageAllocation = """
                This application is a mortgage allocator.\s
                It can be used to assign mortgages belonging to various Landbay products to funders based on the funders' investment preferences.
                To use this application, please follow the instructions below:
                """;

        //print the message above in the command line when the application is opened
        view.display(startMortgageAllocation);

        //Ask user to enter the names of the files needed for the mortgage allocation
        DataFile fileType1 = new DataFile("mortgages");
        DataFile fileType2 = new DataFile("products");
        DataFile fileType3 = new DataFile("funders");

        String mortgages = fileType1.getFileName() + ".csv";
        String products = fileType2.getFileName() + ".csv";
        String funderInfo = fileType3.getFileName() + ".csv";

        //get the location of the data files
        String mortgagesFilePath = getFilePath(mortgages);
        String productsFilePath = getFilePath(products);
        String funderInfoFilePath = getFilePath(funderInfo);

        //read the csv files
        Reader mortgageReader = new Reader(mortgagesFilePath);
        Reader productReader = new Reader(productsFilePath);
        Reader funderReader = new Reader(funderInfoFilePath);

        //create the hashMaps with the records from each file
        HashMap<String, List<String>> mortgageRecords = mortgageReader.getData();
        HashMap<String, List<String>> productRecords = productReader.getData();
        HashMap<String, List<String>> funderRecords = funderReader.getData();

        //calculate total loan value needed to be funded
        float totalLoanVal = getTotalLoanValue(mortgageRecords);

        //get the loan amount available in each product
        HashMap<String, Integer> productLoan;
        productLoan = getProdLoanAmount(mortgageRecords);

        //get the HashMap with the number of funds willing to invest in each product
        HashMap<String, Integer> prodFunders;
        prodFunders = getProdFunderNo(funderRecords);

        //create the HashMap with the mortgage allocation
        HashMap<String, List<String>> allocations = new HashMap<>();
        int totalAllocatedAmount = 0;
        int allocatedMortgages = 0;
        for (String fund: funderRecords.keySet()) {
            List<String> prodOptions = funderRecords.get(fund);
            for (String p : prodOptions) {
                if (productLoan.get(p) != null){
                    int availableToFund = productLoan.get(p) / prodFunders.get(p);
                    for (String mortgage : mortgageRecords.keySet()) {
                        List<String> mortgageDetails = mortgageRecords.get(mortgage);
                        if (p.equals(mortgageDetails.get(1))) {
                            if (availableToFund >= Integer.parseInt(mortgageDetails.get(0))) {
                                //check if key already is in the HashMap
                                List<String> newValue = new ArrayList<>();
                                if (allocations.containsKey(fund)) {
                                    List<String> oldValue = allocations.get(fund);
                                    newValue.addAll(oldValue);
                                }
                                newValue.add(mortgage);
                                allocations.put(fund, newValue);
                                availableToFund -= Integer.parseInt(mortgageDetails.get(0));
                                totalAllocatedAmount += Integer.parseInt(mortgageDetails.get(0));
                                allocatedMortgages++;
                                mortgageDetails.set(1, "");
                                mortgageRecords.replace(mortgage, mortgageDetails);
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        //assign any left mortgages randomly among the funders willing to invest in them
        for (String mortgage : mortgageRecords.keySet()) {
            List<String> mortgageDetails = mortgageRecords.get(mortgage);
            List<String> possibleFunders = new ArrayList<>();
            String prodCode = mortgageDetails.get(1);
            if (!prodCode.equals("")) {
                for (String fund : funderRecords.keySet()) {
                    List<String> prodOptions = funderRecords.get(fund);
                    if (prodOptions.contains(prodCode)) {
                        possibleFunders.add(fund);
                    }
                }
                if (possibleFunders.size() >0 ){
                    Random rand = new Random();
                    String randomElement = possibleFunders.get(rand.nextInt(possibleFunders.size()));
                    List<String> oldValue = allocations.get(randomElement);
                    List<String> newValue = new ArrayList<>(oldValue);
                    newValue.add(mortgage);
                    allocations.put(randomElement, newValue);
                    totalAllocatedAmount += Integer.parseInt(mortgageDetails.get(0));
                    allocatedMortgages++;
                    mortgageDetails.set(1, "");
                }
            }
        }

        System.out.println("A possible allocation of available mortgages to potential funders is:");
        for(String all: allocations.keySet()){
            System.out.println(all + ": " + allocations.get(all));
        }
        float performance = totalAllocatedAmount/totalLoanVal*100;
        //show the performance of the mortgage allocation in terms of number of mortgages & total value allocated
        System.out.println("The total loan amount allocated to various funders is: £" + totalAllocatedAmount);
        System.out.println(df.format(performance) + "% of the total loan value available has been allocated");
        System.out.println("The total number of mortgages allocated to various funders is: " + allocatedMortgages);
    }
}