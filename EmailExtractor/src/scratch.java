public class scratch {
  public static void main (String[] args) {
    String word = "Hi~";
    String newWord = word.replaceAll("[^\\w]","").toLowerCase();
    System.out.println(newWord);
  }
}
