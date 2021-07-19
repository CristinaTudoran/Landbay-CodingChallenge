## Mortgage Allocator for Landbay - Java code challenge

### The application was built using Maven.

### 1. How to run the application

In order to run the Mortgage Allocator application, please ensure the .csv files containing the necessary data are located in the recources folder (e.g. directory_name\src\main\resources). 

Open the project in a Java IDE and run the Main class. 

The user needs to enter the names of the files containing the mortgage, products and funder+product data, excluding the .csv extension. If the file names are not entered correctly, or the files are not located in the resoruces folder as menioned above, the program will throw an error. After entering the last file name, the program will allocate the mortgages to the given funders. The results are displayed in the form Fund_Name: list with mortgages codes which have been allocated to the respective fund. Some metrics of the allocation performance are also provided e.g. the total amount of mortgage loans distributed to the funders, the percentage of the total amount needing funding and the total number of mortgages which have been allocated. 

### 2. Definition of 'fairness'

To ensure a fair distribution among the funders willing to invest in the same product(s), the following approach was used:
- the total value of the mortgages available in a product was split by the number of funders willing to invest in it; this value cannot be surpassed when allocating the mortgages
- if, after allocating mortgages to all funders, there still are mortgages available, each of the available mortgages is randomly allocated to one of the funders wanting to invest in the product the mortgage is part of because this seemed to be the fairest. 

An alternative implementation of 'fairness' which I thought about was ensuring the funders get an equal amount of the total mortgage value on offer. However, this approach presents the possibility of having a funder 'stuck' with investing only in one or two products they selected, which seemed unfair to me. Therefore, I tried to implement a solution which gives each funder exposure to as many of the products they selected as possible. 

### 3. Solution approach

When I first read this problem, I immediately thought about formulating it as an optimisation problem, with the objective function being maximising the number of mortgages allocated. A different objective function could also be maximising the total value allocated to the funders. The problem constraints are the fact that not all funders fund all products and not all products have a funder. Additionally, implementing 'fairness' is another constraint whichh needs to be addressed. Thus, given the problem's nature, I attempted to implement a solution which addresses most of these aspects. 

### 4. Improvements 

A. Avoid high concentration of mortgages in any specific location: a similar approach with the one above could be used to tackle this aspect of the problem, by equally splitting the number of mortgages in a specific location to the funders interested in the products those are part of. 

B. Mortgage funding through one funder rather than another: in this case, splitting the mortgage value up for funding equally among funders would be replaced by the percentages Landbay is willing to allocate each funder. 

C. Other improvements: 
- while this application addresses the problem of one comma being present in a record (e.g. the name of a product), it cannot handle the cases when there are more than one comma present. A way to improve the application is to find a general solution to handle this issue. 
- the appliation could also be run by running the MortgageAlocator configuration. However, this will throw an error 'file cannot be read' due to the presence of a '!' in the file path. Although I tried to fix this problem, I haven't succeeded yet. 
- the application depends on the order of the fields in the .csv files. If the columns are in a different order than the one in the given files, the application will not function as expected. 
