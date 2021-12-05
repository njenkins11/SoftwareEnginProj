package mvc.model;
import java.util.ArrayList;
import java.util.List;

public class FetchResults {
    private int pageSize;
    private int currentPage;
    private long maxElements;
    private int maxPage;
    private ArrayList<Person> persons;

    public FetchResults(int pageSize, int currentPage, long maxElements, ArrayList<Person> persons){
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.persons = persons;
        this.maxElements = maxElements;
        calculateMaxPages();
    }

    public void calculateMaxPages(){
        maxPage = (int) Math.ceil(maxElements / (pageSize * 1.0));
    }

    public boolean validateNextPage(){
        return (currentPage+1 < maxPage + 1);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public ArrayList<Person> getPersons() {
        return persons;
    }

    public void setPersons(ArrayList<Person> persons) {
        this.persons = persons;
    }

    public long getMaxElements() {
        return maxElements;
    }

    public void setMaxElements(long maxElements) {
        this.maxElements = maxElements;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }
}
