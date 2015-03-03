import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class  sequentialCoveringAlgorithm{
    private static final int DEF_DIV_SCALE = 5;

    public static void main(String[] args) throws Exception {
        // Read the database
        File attrNameFile = new File("loan-attr.txt");
        File fileData = new File("loan-train.txt");
        String[] attrNames = readAttrNames(attrNameFile);
        // Generate rule for Loan Training Set
        ArrayList<Item> loanTrain = createRuleGrowingSet(fileData, attrNames);
        ArrayList<Item> postRG = createPost(loanTrain);
        Rule rule = sequential_covering(postRG,loanTrain);
        rule.setName(postRG.get(0).getName());
        System.out.println("\n" + "The loan training rule set without pruning:");
        rule.print();
        ArrayList<Item> restRG = createRest(loanTrain);
        Rule rule1 = sequential_covering(restRG,loanTrain);
        rule1.setName(restRG.get(0).getName());
        rule1.print();

        System.out.println("\n\n"+"After pruning...");
        ArrayList<Item> loanValidation = createValidationSet(fileData, attrNames);
        ArrayList<Item> postLoanValidation = createPost(loanValidation);
        Rule loanRule1 = pruningRule(postLoanValidation,loanValidation,rule);
        loanRule1.setName(postRG.get(0).getName());
        loanRule1.print();
        ArrayList<Item> restLoanValidation = createRest(loanValidation);
        Rule loanRule2 = pruningRule(restLoanValidation, loanValidation,rule1);
        loanRule2.setName(restRG.get(0).getName());
        loanRule2.print();

        System.out.println("\n____________________________________________________________________________");

        //Same as Heart Train Set
        attrNameFile = new File("heart-attr.txt");
        fileData = new File("heart-train.txt");
        attrNames = readAttrNames(attrNameFile);
        // Generate rule for Loan Training Set
        ArrayList<Item> heartTrain = createRuleGrowingSet(fileData, attrNames);
        ArrayList<Item> postheart = createPost(heartTrain);
        Rule rulepostheart = sequential_covering(postheart,heartTrain);
        rulepostheart.setName(postheart.get(0).getName());
        System.out.println("\n"+"The heart training rule set without pruning");
        rulepostheart.print();
        ArrayList<Item> restheart = createRest(heartTrain);
        Rule rulerestheart = sequential_covering(restheart,heartTrain);
        rulerestheart.setName(restheart.get(0).getName());
        rulerestheart.print();
        System.out.println("\n\n"+"After pruning...");
        ArrayList<Item> heartValidation = createValidationSet(fileData, attrNames);
        ArrayList<Item> postHeartValidation = createPost(heartValidation);
        Rule heartRule1 = pruningRule(postHeartValidation,heartValidation,rulepostheart);
        heartRule1.setName(postheart.get(0).getName());
        heartRule1.print();
        ArrayList<Item> restHeartValidation = createRest(heartValidation);
        Rule heartRule2 = pruningRule(restHeartValidation, heartValidation,rulerestheart);
        heartRule2.setName(restheart.get(0).getName());
        heartRule2.print();


    }

    /**
     * Translate Object to Map, return Map
     */
    public static String[] readAttrNames(File attrNameFile)throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(attrNameFile));
        String attrNamesLine;
        int size=0;
        List<String> attrName1 = new ArrayList<String>();
        while((attrNamesLine = in.readLine()) != null){
            List<String> attrName = new ArrayList<String>();
            if(attrNamesLine.split("\t")[0].split(" ")[1].equalsIgnoreCase("continuous")){
                //Mark the continuous attribute
                attrNamesLine.split("\t")[0].split(" ")[0].concat("bv");
            }
            attrName.add(attrNamesLine.split("\t")[0].split(" ")[0]);
            attrName1.add(attrName.toString());
        }
        in.close();
        size=attrName1.size()-1;
        String[] attributeNames = new String[size];
        for(int i=0;i<size;i++){
            attributeNames[i] = attrName1.get(i);
        }
        return attributeNames;
    }

    public static ArrayList<Item> createRuleGrowingSet(File file,String[] attrNames)throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<Item> base_items = new ArrayList<Item>();
        while((line = in.readLine()) != null){
            String[] temp = line.split("\t");
            Item one_item = new Item();
            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
            for(int j=0;j<temp.length;j++) {
                for(int i =0; i < temp[j].split(" ").length;i++){
                    if(i<attrNames.length){
                        Attribute one_attr = new Attribute(attrNames[i],temp[j].split(" ")[i]);
                        attributes.add(one_attr);
                    }
                    else{
                        one_item.init(temp[j].split(" ")[i],attributes);
                    }
                }
                base_items.add(one_item);
            }
        }
        in.close();
        return base_items;
    }
    public static ArrayList<Item> createValidationSet(File file,String[] attrNames)throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(file));
        BufferedReader in1 = new BufferedReader(new FileReader(file));
        String line;
        int count =0,k=0;
        ArrayList<Item> base_items = new ArrayList<Item>();
        while((line = in1.readLine()) != null) {
            String[] temp1 = line.split("\t");
            count++;
            //System.out.println(count);
        }
        in1.close();
        while((line = in.readLine()) != null){
            String[] temp = line.split("\t");
            Item one_item = new Item();
            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
            if(k >= count*0.7) {
                for (int j = 0; j < temp.length; j++) {
                    for (int i = 0; i < temp[j].split(" ").length; i++) {
                        if (i < attrNames.length) {
                            Attribute one_attr = new Attribute(attrNames[i], temp[j].split(" ")[i]);
                            attributes.add(one_attr);
                        } else {
                            one_item.init(temp[j].split(" ")[i], attributes);
                        }
                    }
                    base_items.add(one_item);
                }
            }
            k++;
            //System.out.println(k);
        }
        in.close();
        return base_items;
    }


    public static ArrayList<Item> createPost(ArrayList<Item> allData){
        ArrayList<Item> post = new ArrayList<Item>();
        ArrayList allclasslabel = new ArrayList();
        int countYes=0,countNo=0;
        for(int i =0; i< allData.size();i++){
            allclasslabel.add(allData.get(i).getName());
        }
        for(int i =0; i< allData.size();i++){
            if(allclasslabel.get(i).toString().equalsIgnoreCase("yes")){
                countYes++;
            }
            else{
                countNo++;
            }
        }
        //order set of classes from less frequent to more frequent classes
            if(countNo >= countYes){
                for(int i =0; i< allData.size();i++){
                    if(allData.get(i).getName().equalsIgnoreCase("yes")){
                        post.add(allData.get(i));
                    }
                }
            }
            else{
                for(int i =0; i< allData.size();i++){
                    if(allData.get(i).getName().equalsIgnoreCase("no")){
                        post.add(allData.get(i));
                    }
                }
            }
        return post;
    }

    public static ArrayList<Item> createRest(ArrayList<Item> allData){
        ArrayList<Item> rest = new ArrayList<Item>();
        ArrayList allclasslabel = new ArrayList();
        int countYes=0,countNo=0;
        for(int i =0; i< allData.size();i++){
            allclasslabel.add(allData.get(i).getName());
        }
        for(int i =0; i< allData.size();i++){
            if(allclasslabel.get(i).toString().equalsIgnoreCase("yes")){
                countYes++;
            }
            else{
                countNo++;
            }
        }
        //order set of classes from less frequent to more frequent classes
        if(countNo >= countYes){
            for(int i =0; i< allData.size();i++){
                if(allData.get(i).getName().equalsIgnoreCase("no")){
                    rest.add(allData.get(i));
                }
            }
        }
        else{
            for(int i =0; i< allData.size();i++){
                if(allData.get(i).getName().equalsIgnoreCase("yes")){
                    rest.add(allData.get(i));
                }
            }
        }
        return rest;
    }

    // Computes the rule covering all the examples
    public static Rule sequential_covering(ArrayList<Item> post, ArrayList<Item> allData) {
        Rule result = new Rule();
        ArrayList<Candidate> examples = new ArrayList<Candidate>();
        ArrayList<Candidate> tempSet = new ArrayList<Candidate>();
        //get candidate on all positive examples
        for(int i =0; i<post.size();i++){
            Candidate one_candidate = new Candidate();
            one_candidate.init(post.get(i).getAttributes());
            examples.add(one_candidate);
        }
        //get candidate on all training set
        for(int i =0; i<allData.size();i++){
            Candidate one_candidate1 = new Candidate();
            one_candidate1.init(allData.get(i).getAttributes());
            tempSet.add(one_candidate1);
        }
        ArrayList<AttributeValues> attributes = new ArrayList<AttributeValues>();
        //ArrayList<AttributeValues> attributesBinary = new ArrayList<AttributeValues>();
        // for each element of the Temp Set
        for (int i = 0; i < examples.size(); i++) {
            // for each attribute in candidate
            for (int j = 0; j < examples.get(i).attributes().size(); j++) {
                boolean found = false;
                int lokation = 0;
                String name = examples.get(i).attributes().get(j).getName();
                String value = examples.get(i).attributes().get(j).getValue();
                // for each attribute in list of all attributes
                for (int k = 0; k < attributes.size(); k++) {
                    if (name.compareTo(attributes.get(k).getName()) == 0) {
                        found = true;
                        lokation = k;
                        break;
                    }
                }
                if (found) { // Inserting a value in list of values
                    attributes.get(lokation).insetValue(value);
                } else { // Adding a new attribute
                    ArrayList<String> newvalues = new ArrayList<String>();
                    newvalues.add(value);
                    AttributeValues new_attr = new AttributeValues(name, newvalues);
                    attributes.add(new_attr);
                }
            }
        }
        Conjunction conj = learn_one_rule(attributes, 0.5, examples, tempSet);
        //computeGeneralizationError(first70%conj,last30%allData)
        //whether it met stopping condition
        while (examples.size() != 0) {
            result.getConjunctions().add(conj);
            //removing examples covered by the rule
            Rule one_rule = new Rule();
            one_rule.getConjunctions().add(conj);
            for (int i = examples.size() - 1; i >= 0; i--) {
                if (is_covering(examples.get(i), one_rule)) {
                    examples.remove(i);
                }
            }
            conj = learn_one_rule(attributes, 0.5, examples,tempSet);
        }
        // pruning rule set

        return result;
    }

    // Learns one conjunction to rule
    public static Conjunction learn_one_rule(ArrayList<AttributeValues> attributes, double min_thresh, ArrayList<Candidate> examples,ArrayList<Candidate> allData) {
        Conjunction result = new Conjunction();
        while (attributes.size() != 0) {
            Conjunction child = new Conjunction(result);
            String selected = new String();
            // checking every "result"'s child conjunctions by Laplace and generalization error
            // for each attribute in list
            for (int i = 0; i < attributes.size(); i++) {
                // for each value in list of attribute's values
                for (int j = 0; j < attributes.get(i).getValues().size(); j++) {
                    //attributes.get(i).print();
                    ArrayList<Conjunction> conj = new ArrayList<Conjunction>();
                    ArrayList<Conjunction> res_conj = new ArrayList<Conjunction>();
                    Attribute one_attr = new Attribute(
                            attributes.get(i).getName(),
                            attributes.get(i).getValues().get(j));
                    child.getAttr().add(one_attr);
                    conj.add(child);
                    res_conj.add(result);
                    Rule one_rule = new Rule(conj);
                    Rule res_rule = new Rule(res_conj);
                    double L_or = computeLaplace(one_rule, allData, examples);
                    double L_rr = computeLaplace(res_rule, allData, examples);
                    double GE_or = computeGeneralizationError(one_rule, examples);
                    if ((L_or > L_rr) && (GE_or >= min_thresh)) {
                        result.copy(child);
                        selected = one_attr.getName();
                    }
                    child.getAttr().remove(child.getAttr().size() - 1);
                }
            }
            // removing selected attribute from list
            if (selected.compareTo("") == 0) {
                break;
            } else {
                for (int i = 0; i < attributes.size(); i++) {
                    if (attributes.get(i).getName().compareTo(selected) == 0) {
                        attributes.remove(i);
                        break;
                    }
                }
            }
        }
        //result.print();
        return result;
    }

    // Checking covering candidate by rule
    public static boolean is_covering(Candidate cand, Rule rule) {
        boolean[] flag1 = new boolean[rule.getConjunctions().size()];
        for (int i = 0; i < flag1.length; i++) {
            flag1[i] = true;
        }
        // For each conjunction in rule
        for (int i = 0; i < rule.getConjunctions().size(); i++) {
            boolean flag2 = true;
            Conjunction conj = rule.getConjunctions().get(i);
            // For each attribute in conjunction
            for (int j = 0; j < conj.getAttr().size(); j++) {
                String attr = cand.search(conj.getAttr().get(j).getName());
                if ((attr == null) || (attr.compareTo(
                        conj.getAttr().get(j).getValue()) != 0)) {
                    flag2 = false;
                    break;
                }
            }
            if (!flag2) {
                flag1[i] = false;
                break;
            }
        }
        for (int i = 0; i < flag1.length; i++) {
            if (flag1[i]) {
                return true;
            }
        }
        return false;
    }

    // Computes Laplace to evaluate rule
    public static double computeLaplace(Rule rule, ArrayList<Candidate> allDataSet, ArrayList<Candidate> postDataSet) {
        double postcover = 0.0;
        double examplecover = 0.0;
        // counting f+ and n
        for (int i = 0; i < postDataSet.size(); i++) {
            if (is_covering(postDataSet.get(i), rule)) {
                postcover += 1;
            }
        }
        for (int j = 0; j < allDataSet.size(); j++) {
            if (is_covering(allDataSet.get(j), rule)) {
                examplecover += 1;
            }
        }
        return (1+postcover)/(examplecover+2);
    }

    // Computes generalization error
    public static double computeGeneralizationError(Rule rule, ArrayList<Candidate> validationSet) {
        double truepositives = 0.0;
        // for each item in validation set
        for (int i = 0; i < validationSet.size(); i++) {
            // Checking the candidate by rule
            if (is_covering(validationSet.get(i), rule)) {
                truepositives += 1;
            }
        }
        return truepositives/validationSet.size();
    }

    public static Rule pruningRule(ArrayList<Item> post, ArrayList<Item> allData, Rule rule) {
        Rule tempRule = new Rule();
        ArrayList<Candidate> examples = new ArrayList<Candidate>();
        ArrayList<Candidate> tempSet = new ArrayList<Candidate>();
        //get candidate on all positive examples
        for(int i =0; i<post.size();i++){
            Candidate one_candidate = new Candidate();
            one_candidate.init(post.get(i).getAttributes());
            examples.add(one_candidate);
        }
        //get candidate on all training set
        for(int i =0; i<allData.size();i++){
            Candidate one_candidate1 = new Candidate();
            one_candidate1.init(allData.get(i).getAttributes());
            tempSet.add(one_candidate1);
        }
        for(int j=0; j<rule.getConjunctions().size();j++){
            tempRule.getConjunctions().add(rule.getConjunctions().get(j));
            double L_rs = computeLaplace(tempRule,tempSet,examples);
            double GE = 1.0 - L_rs;
        //whether it met stopping condition
        if (GE< 0.5) {
            rule.getConjunctions().remove(j);
        }
        }
        return rule;
    }

    /**
     * define the data structure
     */
    static class Attribute {
        private String name;
        private String value;
        public Attribute() {
            name = new String();
            value = new String();
        }
        public Attribute(String new_name, String new_value) {
            name = new_name;
            value = new_value;
        }
        // Shows attributes's fields
        public String getName() { return name;}
        public String getValue() { return value;}

        // Prints one attribute "key value"
        public void print() {
            System.out.print(name.concat(" ").concat(value));
        }
    }

    static class Candidate {
        private ArrayList<Attribute> attributes;
        public Candidate() {
            attributes = new ArrayList<Attribute>();
        }
        // Initializing candidate
        public void init(ArrayList<Attribute> initial) {
            for (int i = 0; i < initial.size(); i++) {
                attributes.add(initial.get(i));
            }
        }
        // Return list of attributes
        public ArrayList<Attribute> attributes() {
            return attributes;
        }
        // Return value of the attribute with name "name" or 'null' if find nothing
        public String search(String name) {
            for (int i = 0; i < attributes.size(); i++) {
                if (attributes.get(i).getName().compareTo(name) == 0) {
                    return attributes.get(i).getValue();
                }
            }
            return null;
        }
        // Print candidate's attributes
        public void print() {
            for (int i = 0; i < attributes.size(); i++) {
                attributes.get(i).print();
            }
        }
    }

    static class Item {
        private String classlabel;
        private ArrayList<Attribute> attributes;
        public Item() {
            classlabel = "";
            attributes = new ArrayList<Attribute>();
        }
        // Initialising
        public void init(String newname, ArrayList<Attribute> newattributes) {
            classlabel = newname;
            attributes = new ArrayList<Attribute>();
            for (int i = 0; i < newattributes.size(); i++) {
                attributes.add(newattributes.get(i));
            }
        }
        // Printing item
        public void print() {
            System.out.println(classlabel);
            for (int i = 0; i < attributes.size(); i++) {
                attributes.get(i).print();
            }
        }
        public String getName() {
            return classlabel;
        }
        public ArrayList<Attribute> getAttributes() {
            return attributes;
        }
    }

    static class AttributeList {
        private ArrayList<String> names;
        public AttributeList() {
            names = new ArrayList<String>();
        }
        public AttributeList(ArrayList<Candidate> reference_set) {
            ArrayList<Integer> values = new ArrayList<Integer>();
            names = new ArrayList<String>();
            for (int i = 0; i < reference_set.size(); i++) {
                // for each attribute in candidate
                for (int j = 0; j < reference_set.get(i).attributes().size(); j++) {
                    boolean found = false;
                    int lokation = 0;
                    String name = reference_set.get(i).attributes().get(j).getName();
                    // for each attribute in list of all attributes
                    for (int k = 0; k < names.size(); k++) {
                        if (name.compareTo(names.get(k)) == 0) {
                            found = true;
                            lokation = k;
                            break;
                        }
                    }
                    if (!found) {
                        names.add(name);
                        values.add(1);
                    } else {
                        values.set(lokation, values.get(lokation) + 1);
                    }
                }
            }
        }
        // Returns list of attributes' names
        public ArrayList<String> getNames() {
            return names;
        }
    }

    static class AttributeValues {
        private String name;
        private ArrayList<String> values;
        public AttributeValues() {
            name = "";
            values = new ArrayList<String>();
        }
        public AttributeValues(String newname, ArrayList<String> newvalues) {
            name = newname;
            values = newvalues;
        }

        // Returns list of attribute's values
        public String getName() {
            return name;
        }

        // Returns name of attribute
        public ArrayList<String> getValues() {
            return values;
        }

        // Inserts a new value in list
        public void insetValue(String newvalue) {
            // for each value in list
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).compareTo(newvalue) == 0) {
                    return;
                }
            }
            values.add(newvalue);
        }
        // Prints attribute with list of it's values
        public void print() {
            System.out.println("Attribute : ".concat(name));
            for (int i = 0; i < values.size(); i++) {
                System.out.println("    ".concat(values.get(i)));
            }
            System.out.println("");
        }
    }

    static class Conjunction {
        private ArrayList<Attribute> condition;
        public Conjunction() {
            condition = new ArrayList<Attribute>();
        }
        public Conjunction(Conjunction conj) {
            condition = new ArrayList<Attribute>(conj.getAttr());
        }
        public Conjunction(ArrayList<Attribute> conj) {
            condition = new ArrayList<Attribute>();
            for (int i = 0; i < conj.size(); i++) {
                condition.add(conj.get(i));
            }
        }
        public void copy(Conjunction conj) {
            condition = (ArrayList<Attribute>) conj.getAttr().clone();
        }
        // Printing conjunction
        public void print() {
            for (int i = 0; i < condition.size(); i++) {
                condition.get(i).print();
                if (i!= (condition.size()-1))
                    System.out.print(" ^ ");
            }
        }
        public ArrayList<Attribute> getAttr() { return condition;}
        public void setAttr(ArrayList<Attribute> attr_names) {
            this.condition = attr_names;
        }
    }

    static class Rule {
        private ArrayList<Conjunction> conjunctions;
        private String classlabel;
        public Rule() {
            conjunctions = new ArrayList<Conjunction>();
        }
        public Rule(ArrayList<Conjunction> conj) {
            conjunctions = new ArrayList<Conjunction>();
            for (int i = 0; i < conj.size(); i++) {
                conjunctions.add(conj.get(i));
            }
        }
        public String getName() {
            return classlabel;
        }
        public void setName(String name){this.classlabel = name;}
        public ArrayList<Conjunction> getConjunctions() { return conjunctions;}
        public void setConjunctions(ArrayList<Conjunction> conjunctions) {
            this.conjunctions = conjunctions;
        }
        // Printing rule
        public void print() {

            for (int i = 0; i < conjunctions.size(); i++) {
                System.out.print("\n"+" Rule : ");
                conjunctions.get(i).print();
                System.out.print(" ->" + classlabel);
            }

        }
    }
}