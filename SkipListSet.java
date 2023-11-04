import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.NoSuchElementException;
import java.util.Random;

// Skiplist class
public class SkipListSet<T extends Comparable<T>> implements SortedSet<T>{


    // declare and initialize variables 
    private static final Random rand = new Random();  // used for calcualtions
    private static final int lowLevel = 3;  // iniitaliuz
    private int elNum;  // Number of elements in the list
    private int topLevel;  // Maximum height of list, typically floor(log_2(elNum))
    private boolean align;  // Indicates whether the list is being rebalanced, to avoid sizing issues
    private SkipListItem<T> front;  // front of the skiplist
    

    public SkipListSet(){
        front = new SkipListItem<T>();  // Initialize front
        topLevel = lowLevel;  // Set the maximum height to the static minimum height
        align = false;
        elNum = 0;

      // Create list hat with empty nodes
      SkipListItem<T> walker1 = front;
      for(int levels = 1; levels < topLevel; levels++){
        SkipListItem<T> itemN = new SkipListItem<T>();
        walker1.setLower(itemN); 
        itemN.setHigher(walker1);
        walker1 = walker1.getLower();
      }
    }


  public SkipListSet(Collection<? extends T> cat){
    initRebal();

    // Set the maximum height to the "optimal" height
    topLevel = levelsChecker(cat.size());
    front = makeNode();

    // Create list hat with empty nodes
    makeListH(topLevel, front);

    // Add all collection elements to the list
    addAllEle(cat);
  }

  private void initRebal(){
      align = false;
  }

  private int levelsChecker(int elNum){
      return topLevel(elNum);
  }

  private SkipListItem<T> makeNode(){
      return new SkipListItem<T>();
  }

  private void makeListH(int topLevel, SkipListItem<T> front){
      SkipListItem<T> walkerN = front;
      for(int levels = 1; levels < topLevel; levels++){
          walkerN = connectNewNode(walkerN);
      }
  }

  private SkipListItem<T> connectNewNode(SkipListItem<T> walkerN){
      walkerN.setLower(new SkipListItem<T>()); 
      walkerN.getLower().setHigher(walkerN);
      return walkerN.getLower();
  }

  private void addAllEle(Collection<? extends T> cat){
      addAll(cat);
  }


 // Aligns the skiplist to teh proper implementaiton 
  public boolean reBalance(){
    // aligns the levels if the tree and 
    // resests it to redo the proper height 
    align = true;
    elNum = 0;
    Iterator<T> curItemIterator = new SkipListSetIterator<T>(this);
    clear(); // clear the skip

    // stores and checks the items from the array temporairly an readds it
    List<T> tempListForReAdding = new ArrayList<T>();
    curItemIterator.forEachRemaining(tempListForReAdding::add);
    addAll(tempListForReAdding);
    align = false;

    return true;
}

  /* override for the interface for the accordingly 
   * to the implementation 
   */
  @Override
  public int size() {
    return elNum;
  }

  // Checks if it is empty.
  @Override
  public boolean isEmpty() {
    return elNum == 0;
  }

 
  @Override
  public boolean contains(Object item) {
    T temp = (T)item;
    SkipListItem<T> walker1 = front;

    while(walker1 != null) {
        // traverses the skiplist 
        while(isRhandCheck(temp, walker1)) {
            if(isSameVal(temp, walker1.getRhand())){
              return true;
            }
            walker1 = goRhandCheck(walker1);
        }
        // checks to see if it can go right or lower 
        if(isLowerCheck(walker1)) {
            walker1 = goLowerCheck(walker1);
        } else {
            break; // if it Cannot go right or lower
        }
    }

    
    return false;
  }
    // to see if it can go right or lower 
    private boolean isRhandCheck(T e, SkipListItem<T> walker1){
        return walker1.getRhand() != null && walker1.getRhand().getInfo().compareTo(e) <= 0;
    }
    private SkipListItem<T> goRhandCheck(SkipListItem<T> walker1){
        return walker1.getRhand();
    }
    private boolean isLowerCheck(SkipListItem<T> walker1){
        return walker1.getLower() != null;
    }
    private SkipListItem<T> goLowerCheck(SkipListItem<T> walker1){
        return walker1.getLower();
    }
    private boolean isSameVal(T e, SkipListItem<T> walker1){
        return walker1.getInfo().compareTo(e) == 0;
    }


    // checks the iterator and implements the array accordingly 
  @Override
  public Iterator<T> iterator(){
      return new SkipListSetIterator(this);
  }
  @Override
  public Object[] toArray(){
      List<T> listFromSet = makeSetList();
      return listFromSet.toArray();
  }
  @Override
  public <I> I[] toArray(I[] a){
      List<T> listFromSet = makeSetList();
      if(listFromSet.size() > a.length) {
          return listFromSet.toArray(a);
      }

      // copies the elements and fill the arrays 
      System.arraycopy(listFromSet.toArray(), 0, a, 0, listFromSet.size());
      Arrays.fill(a, listFromSet.size(), a.length, null);

      return a;
  }

  private List<T> makeSetList() {
      List<T> asList = new ArrayList<>();
      iterator().forEachRemaining(asList::add);
      return asList;
}


  @Override
  public boolean add(T item) {
      SkipListItem<T> walker1 = front;
      SkipListItem<T> walker2;
      int valChecker;

      // travses the skip list till the right place is found 
      while (true) {
          // checks to see if we can move right 
          if (walker1.getRhand() != null) {
              valChecker = walker1.getRhand().getInfo().compareTo(item);
              if (valChecker == 0) {
                  return false;
              } else if (valChecker < 0) {
                  walker1 = walker1.getRhand(); // goes to the right 
                  continue;
              }
          }
          
          // checks to see if it can go lower 
          if (walker1.getLower() != null) {
              // goes lower 
              walker1 = walker1.getLower();
              continue;
          }

          walker2 = new SkipListItem<T>(item);
          addLevelsRHand(walker1, walker2);
          elNum++; // updates the value 
          alterLevelsTop(); // adjust levels
          
          return true;
      }
  }


  // adds values to the right side 

  private void addLevelsRHand(SkipListItem<T> walker1, SkipListItem<T> walker2) {
    // stores info
    T item = walker2.getInfo();
    // takes node to right
    walker2.midAdd(walker1, walker1.getRhand());

    // calculates the height for the node 
    int calcLevel = Math.min((int)Math.floor(Math.log(1.0 / rand.nextDouble())/Math.log(2) + 1), topLevel);

    // creates and connects, and increases heigth accordingly 
    for (int levelsC = 1; levelsC < calcLevel; levelsC++) {
        walker2.setHigher(new SkipListItem<>(item));
        walker2.getHigher().setLower(walker2);
        walker2 = walker2.getHigher();
        while(walker1.getHigher() == null) {
            walker1 = walker1.getLhand();
        }
        walker1 = walker1.getHigher();
        walker2.midAdd(walker1, walker1.getRhand());
    }
}

// implementation needed for assignment 
    @Override
    public int hashCode(){
        return 0;
    }
    @Override
    public boolean equals(Object obj){
        return true;
    }

    // checks if the alightment is in progress
  private void alterLevelsTop(){
    if (align) {
        return;
    }
    // Calculate the new level
    int topLevelchecker = topLevel(elNum);

    if (topLevelchecker == topLevel) {
        return;
    }

    // calculates the new height 
    int prevTop = topLevel;
    topLevel = Math.max(topLevelchecker, lowLevel);

    // cehcks to see if old nodes levels needs to be increases 
    if (prevTop < topLevel) {
        int moreLevels = topLevel - prevTop;
        for (int i = 0; i < moreLevels; i++) {
            SkipListItem<T> itemN = new SkipListItem<>();
            itemN.setLower(front);
            front.setHigher(itemN);
            front = itemN;
        }

        // checks to see if the levels needs to be decreases 
    } else if (prevTop > topLevel) {
        
        // decreases hte levels 
        int lessLevels = prevTop - topLevel;
        SkipListItem<T> walkerC = front;

        for (int i = 0; i < lessLevels; i++) {
            // deletes the connections 
            while (walkerC.getRhand() != null) {
                // cehcks to remove horizontal connections 
                walkerC = walkerC.getRhand();
                walkerC.getLhand().setRhand(null);
                walkerC.setLhand(null);

                // remover vertical connections 
                walkerC.getLower().setHigher(null);
                walkerC.setLower(null);
            }

            // Updates the front node 
            front = front.getLower();
            front.getHigher().setLower(null);
            front.setHigher(null);
        }
    }
}

// removes the skip lsit elements in the proper format 
  @Override
  public boolean remove(Object item) {

      if(item == null){
          return false;
      }
    // declares variables 
      T item1 = (T)item;
      SkipListItem<T> walkerN = front;

      // traverses list till it needs to remove something 
      while(true){

          // if there is no rigth node
          if(walkerN.getRhand() == null){

              // can it go lower?
              if(walkerN.getLower() != null){
                  walkerN = walkerN.getLower();
              }else{
                  return false;
              }

          }else if(walkerN.getRhand().getInfo().compareTo(item1) == 0){

            // declares variagbes to walk the right hand side 

              walkerN = walkerN.getRhand();
              while(walkerN.getLower() != null) {
                  // connects right and left nodes 
                  connect(walkerN.getLhand(), walkerN.getRhand());

                  // removees connections 
                  walkerN.setLhand(null);
                  walkerN.setRhand(null);

                  // moves down and removes lower nad higher connections 
                  walkerN = walkerN.getLower();
                  walkerN.getHigher().setLower(null);
                  walkerN.setHigher(null);
              }
              
              // removes and fixes connections and updates number of elements 
              connect(walkerN.getLhand(), walkerN.getRhand());
              walkerN.setLhand(null);
              walkerN.setRhand(null);
              elNum--;
              
              alterLevelsTop();
              return true;

          } else if(walkerN.getRhand().getInfo().compareTo(item1) < 0) { // checkks ot see if we can go to the right 
              walkerN = walkerN.getRhand(); 
          } else if(walkerN.getLower() != null){ // checks to see if we can go to the rigth 
              walkerN = walkerN.getLower();
          } else {
              return false;
          }
      }
  }



    // connects the skip list and connects the skip lsit properly 
    public void connect(SkipListItem<T> handL, SkipListItem<T> handR) {
        if(handL != null) {
            handL.setRhand(handR);
        }
        // Check if the right item is not null and connect it to the left item
        if(handR != null) {
            handR.setLhand(handL);
        }
    }

    // over ride for nessary implementaitons 
    @Override
    public boolean containsAll(Collection<?> cat) {
        Collection<T> incomingCollection = (Collection<T>) cat;
    
        // iterates over the collections 
        for(T element : incomingCollection) {
           // returns false if there is no element in the collection 
            if(!contains(element)) {
                return false;
            }
        }
        // returns true if there is an element in teh colletion 
        return true;
    }
    // override for addlign all the categoreies and elements 
    @Override
    public boolean addAll(Collection<? extends T> cat) {
        // Initalizes variable to see chanfge 
        boolean collectionChange = false;
        for (T element : cat) {
            if (this.add(element)) {
                collectionChange = true;
            }
        }
        return collectionChange;// Return whether if the collection was changed 
    }

  // checks to see if the method contains certian collections 
  @Override
  public boolean retainAll(Collection<?> cat){

    List<T> elementsToRemove = new ArrayList<>();
    iterator().forEachRemaining(elementsToRemove::add);

    
    for(Object thing : cat){
        T element = (T) thing;
        if(contains(element)){
            elementsToRemove.remove(element);
        }
    }
    
    removeAll(elementsToRemove);

    return !elementsToRemove.isEmpty(); // deteremines of the collection is empty or not 
  }


    // removes all elements for the collections 
    // is implemented to our specific purpiose for the assignment 
    @Override
    public boolean removeAll(Collection<?> cat) {
        boolean collectionChange = false;
        for (Object chitem : cat) {
            if (remove(chitem)) {
                collectionChange = true;
            }
        }
        return collectionChange;
    }

    // clears all elements from the collection 
    @Override
    public void clear() {
        List<T> elementsToRemove = new ArrayList<>(); // takes in all teh set 
        iterator().forEachRemaining(elementsToRemove::add);
        // Remove everything 
        removeAll(elementsToRemove);
    }

    // returns null if the cllection does not use a comparator 
    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    /* collections below may not be supported since htey have an error 
     * but are part of the implementation requested 
     */
    @Override
    public SortedSet<T> subSet(T curEle, T nextEle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> headSet(T nextEle) {
        throw new UnsupportedOperationException();
    }

  @Override
  public SortedSet<T> tailSet(T curEle) {
      throw new UnsupportedOperationException();
  }



  // over rides to check the first element and what is essential for the skiplsit 
  @Override
  public T first() {
      SkipListItem<T> itemC = front;
      while(itemC.getLower() != null) {
          itemC = itemC.getLower();
      }
      if(itemC.getRhand() == null) {
          throw new NoSuchElementException();
      }
      return itemC.getRhand().getInfo();
  }


  // returns the highest element in teh collection 
  @Override
  public T last() {
      SkipListItem<T> itemC = front;

      while (true) {
          if (itemC.getRhand() == null){
              if (itemC.getLower() != null) {
                  itemC = itemC.getLower();
                  continue;
              }
              if (itemC.getInfo() == null) {
                  throw new NoSuchElementException();
              }
              return itemC.getInfo();
          }
          itemC = itemC.getRhand();
      }
  }


    // creates the wrapper for the skiplist 
  private class SkipListItem<T extends Comparable<T>> {
    // declares variables 
    private T info;  
    private SkipListItem<T> lhand;  // Pointer node left
    private SkipListItem<T> rhand; // Pointer node right
    private SkipListItem<T> higherLevels;    // Pointer node high
    private SkipListItem<T> lowerLevels;  // Pointer node low

    // below are default constructors if null or if there is info 
    public SkipListItem() {
        this.info = null;
    }
    public SkipListItem(T info) {
        this.info = info;
    }

    // helps aids in the access other members in teh skiplsit 

    public SkipListItem<T> getLhand() { return lhand; }
    public void setLhand(SkipListItem<T> node) { this.lhand = node; }

    public SkipListItem<T> getRhand() { return rhand; }
    public void setRhand(SkipListItem<T> node) { this.rhand = node; }

    public SkipListItem<T> getHigher() { return higherLevels; }
    public void setHigher(SkipListItem<T> node) { this.higherLevels = node; }

    public SkipListItem<T> getLower() { return lowerLevels; }
    public void setLower(SkipListItem<T> node) { this.lowerLevels = node; }

    // gets hte info stores in teh node 
    public T getInfo() { return info; }

    // inserts nodes between other nodes 
    public void midAdd(SkipListItem<T> lhand, SkipListItem<T> rhand){
        this.setRhand(rhand);
        this.setLhand(lhand);
        lhand.setRhand(this);
        if(rhand != null) {
            rhand.setLhand(this);
        }
    }
  }

    class SkipListSetIterator<I extends Comparable<I>> implements Iterator<I>{

        private SkipListItem<I> walker; // helps walk the node, is the live node 
        private SkipListItem<I> prevNode; // Points to previous nodes 

        // constructor class 
        SkipListSetIterator(SkipListSet<T> set){
            SkipListItem<I> walker1 = (SkipListItem<I>) set.front;

            // Move to the bottom 
            while (walker1.getLower() != null){
                walker1 = walker1.getLower();
            }

            // helps create the node 
            walker = new SkipListItem<I>(walker1.getInfo());
            SkipListItem<I> walker2 = walker;

            // walks trough the skiplist 
            while (walker1.getRhand() != null){
                walker1 = walker1.getRhand();
                SkipListItem<I> newNode = new SkipListItem<I>(walker1.getInfo());
                walker2.setRhand(newNode);
                walker2 = newNode;
            }
        }

        // the override for the skiplist for the purpose of the assignment 
        // checks to see if the itertor as more elements 
        @Override
        public boolean hasNext(){
            if (walker != null){
                return walker.getRhand() != null;
            }
            throw new NoSuchElementException();
        }

        // next elements 
        @Override
        public I next(){
            if (walker.getRhand() == null){
                throw new NoSuchElementException();
            }
            prevNode = walker;
            walker = walker.getRhand();
            return walker.getInfo();
        }

        // removes all elements from the set, uses the iterator 
        // is not compatable with al teh sets 
        @Override
        public void remove(){
            if (prevNode == null) {
                throw new IllegalStateException();
            }

            // checkks to see if ou can move rigth and sets it 
            prevNode.setRhand(walker.getRhand());
            walker = prevNode; //moces right 
            // reserts the node 
            prevNode = null;
        }
    }
    
    // calcualtes the top level 
    public static int topLevel(int num){
        double val = Math.log(2); // uses hte equation for log 2 to determine the levels 
        return (int)Math.floor(Math.log(num) / val);
    }

}

