/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers. costructor cost O(1)
 */
public class FibonacciHeap
{
    private HeapNode min;
    private HeapNode first;
    private static int linkCount;
    private static int cutCount;
    private int size;
    private int notMarkedNodes;

    private int numTrees;

   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty. O(1)
    *   
    */
    public boolean isEmpty()
    {
        if (this.size == 0) {
            return true;
        }
    	return false;
    }

		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The added key is assumed not to already belong to the heap.  
    * 
    * Returns the newly created node.   O(1)
    */
    public HeapNode insert(int key)
    {
        HeapNode newNode = new HeapNode(key);
        if (this.isEmpty()) {
            this.min = newNode;
            this.first = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
        }
        else{
            this.inNewLeftTree(newNode);
        }
        this.size += 1;
        this.notMarkedNodes++;
        this.numTrees++;
        return newNode;
    }

    /**
     * private void inNewLeftTree(HeapNode newRoot)
     *
     * puts a sub-tree with newRoot as root. the
     * new sub-tree added at left side of heap   O(1)
     *
     */
    private void inNewLeftTree(HeapNode newRoot) {
        HeapNode temp = this.first;
        newRoot.next = temp;
        newRoot.prev = temp.prev;
        temp.prev.next = newRoot;
        temp.prev = newRoot;
        this.first =  newRoot;

        if (newRoot.getKey() < this.min.getKey()){
            this.min = newRoot;
        }

    }
    /**
     * public HeapNode getFirst()
     *
     * returns the root of the most left tree of the heap.   O(1)
     *
     */

    public HeapNode getFirst(){
        return this.first;
    }

   /**
    * public void deleteMin()
    *
    * Deletes the node containing the minimum key.  WC:O(n) AMORT: O(log n) n - number of trees
    *
    */
    public void deleteMin()
    {
        this.notMarkedNodes-=1;
        if (this.min.rank > 0) {
            if (this.min == this.first) {
                this.first = this.min.child;
            }
            if (this.min.next==this.min) {
                this.min.child.prev.next = this.min.child;
            }
            else
            {
                this.min.child.prev.next = this.min.next;
                this.min.child.prev = this.min.prev; //edit!!!!!!
            }
            this.min.prev.next = this.min.child;
            this.min.next.prev = this.min.child.prev;
            HeapNode currentNode = this.min.child;
            do {
                currentNode.parent = null;
                if (currentNode.mark)
                {
                    currentNode.mark = false;
                    this.notMarkedNodes+=1;

                }

                currentNode = currentNode.next;
            } while (currentNode != this.min.child);
            this.min.child = null;
        }
        else {
            if (this.min == this.first) {
                if (this.min.next!=this.min)
                {
                    this.first = this.min.next;
                }
                else
                {
                    this.first = null;
                }
            }
            this.min.prev.next = this.min.next;
            this.min.next.prev = this.min.prev;
            this.numTrees-=1;
        }
        this.size--;
        Consolidate();
    }
    /**
     * private void Consolidate()
     *
     * helping-function for delete. puts the trees in 'buckets' by their rank. WC: O(n)
     *
     */

    private void Consolidate() {

        if (this.isEmpty())
            return;
        HeapNode[] bucketsList = new HeapNode[1 + 2 * (int) (Math.ceil(Math.log10(this.size) / Math.log10(2)))];
        HeapNode currNode = this.first;
        do {

            HeapNode NodeNext = currNode.next;
            //we want to break the subTree we look at now
            //from the General Heap
            SetApart(currNode);
            if (bucketsList[currNode.rank] == null) {
                // there's still nothing in bucket
                // we just put it in
                bucketsList[currNode.rank] = currNode;
            }
            else
            {

                SuccessiveLinking(currNode, bucketsList);
            }
            // we want to iterate next after
            currNode = NodeNext;
        // we do that in a loop until we make a Circle
        }
        while (currNode != this.first) ;
        // now we want to set our first pointer to
        // the smallest tree in the bucketList
      for (int k =0; k <bucketsList.length; k++) {
            if (bucketsList[k] != null) {
               this.first = bucketsList[k];
                break;
          }
       }
        this.numTrees = 0;
        // we now want to iterate backwards, from biggest to smallest
        // and start adding up our little trees to a heap!
        // already collected the "last" possible tree, so we start

        for (int k = 0; k <bucketsList.length; k++) {
            if (bucketsList[k] == null) {
                continue;
                //meaning there's no tree to collect
            } else {
                 this.AddToHeap(bucketsList[k]);
            }
        }
        this.updateMin();
    }

    /**
     * private void SetApart(HeapNode node)
     *
     * helping-function for delete. sets node apart of the tree. the sub-tree of the node stays conected. O(1)
     *
     */

    private void SetApart(HeapNode node) {
        node.prev = node;
        node.next = node;
    }
    /**
     * private void SuccessiveLinking(HeapNode heap,HeapNode[] bucketsList)
     *
     * helping-function for delete. Makes successive linking, recursively goes over the 'buckets' and
     * linking the trees in it using function 'linkHeaps'.    WC: O(n).  Amort: O(log n)
     *
     */


    private void SuccessiveLinking(HeapNode heap,HeapNode[] bucketsList) {
        if (bucketsList[heap.rank] == null)
            // we put heap in bucket if it's empty
            bucketsList[heap.rank] = heap;
        else {
            int rank = heap.rank;
            HeapNode TwoLinkedHeaps = linkHeaps(heap, bucketsList[rank]);
            // after linking we want to update the current
            // bucket which will turn to be empty
            bucketsList[rank] = null;
            //then we want to continue with reccursion of the
            // new Linked heap from the 2 heaps we had before
            SuccessiveLinking(TwoLinkedHeaps, bucketsList);
        }
    }
    /**
     * private HeapNode linkHeaps(HeapNode heap1, HeapNode heap2)
     *
     * helping-function for delete. Given two trees, linking them to one tree. The smaller root will be the
     * new root. The second tree will be its new child using function 'AddAnotherChild' from HeapNode class .
     *
     * returns the new root of the two linked trees (the smaller root). O(1)
     */

    private HeapNode linkHeaps(HeapNode heap1, HeapNode heap2)
    {
        this.linkCount+=1;
        if(heap1.getKey()<heap2.getKey())
        {
            heap1.AddAnotherChild(heap2);
            return heap1;
        }
        else {
            heap2.AddAnotherChild(heap1);
            return heap2;
        }
    }
    /**
     * private void AddToHeap(HeapNode heap)
     *
     * helping-function for delete. Adds the tree to the general heap. O(1)
     *
     *
     */
    private void AddToHeap(HeapNode heap)
    {

        if (this.isEmpty())
        {
            this.first = heap;

        }
        // else: not empty
        heap.next = this.first;
        heap.prev = this.first.prev;
        this.first.prev = heap;
        heap.prev.next = heap;
        this.numTrees++;
    }

    /**
     * private void updateMin()
     *
     * helping-function for delete. Goes over the tree roots and finds the new minimun. O(log n)
     *
     */
    private void updateMin()
    {

        if (this.isEmpty())
            return ;
        HeapNode currNode = this.first;
        HeapNode NewMin = currNode;
        do {
            if(currNode.getKey()< NewMin.getKey())
                NewMin = currNode;
            currNode = currNode.next;
        }
        while(currNode!=this.first);
        // we iterate in a circle, looking for our new min
        this.min = NewMin;
    }

    /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty. O(1)
    *
    */
    public HeapNode findMin()
    {
        if (this.isEmpty()){
            return null;
        }
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Melds heap2 with the current heap.   O(1)
    *
    */
    public void meld (FibonacciHeap heap2)
    {
        if (heap2.isEmpty()){
            return;
        }
        if(this.isEmpty()){
            this.first = heap2.first;
            this.min= heap2.findMin();
            this.size = heap2.size();
            this.notMarkedNodes = heap2.nonMarked();
            this.numTrees = heap2.numTrees;
            return;
        }
       HeapNode secondRoot = heap2.first;
       this.first.prev.next = secondRoot;
       HeapNode lastTree2  = secondRoot.prev;
       secondRoot.prev = this.first.prev;

       this.first.prev = lastTree2;
       lastTree2.next = this.first;
       if (heap2.findMin().getKey() < this.findMin().getKey()){
           this.min = heap2.findMin();
       }
       this.size = this.size() + heap2.size();
       this.notMarkedNodes+= heap2.nonMarked();
       this.numTrees+= heap2.numTrees;
        return;
    }

   /**
    * public int size()
    *
    * Returns the number of elements in the heap.  O(1)
    *   
    */
    public int size()
    {
        return this.size;

    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * (Note: The size of the array depends on the maximum order of a tree.)  O(log n)
    * 
    */
    public int[] countersRep()
    {
        int[] emptyArr = new int[0];
        if (this.isEmpty()){
            return emptyArr;
        }
        HeapNode curr = this.first;
        int max = 0;
        do{
            if (curr.rank > max){
                max = curr.rank;
            }
            curr = curr.next;
        }
        while (curr != this.first);

         int[] arr = new int[max+1];
         HeapNode first = this.first;
         curr = this.first;
         curr = curr.next;
         arr[first.rank] += 1;
         while( curr != first ){
             arr[curr.rank] += 1;
             curr = curr.next;
       }
         return arr;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.   WC: O(n).  Amort: O(log n)
    *
    */
    public void delete(HeapNode x) 
    {
        this.decreaseKey(x, Integer.MIN_VALUE);    //WC: O(log n).  Amort: O(1)
        this.deleteMin();                          //WC: O(n).  Amort: O(log n)
    	return;
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed). O(logn)
    */
    public void decreaseKey(HeapNode x, int delta)
    {
        if (delta == Integer.MIN_VALUE){
            x.key = Integer.MIN_VALUE;
        }
        else {
            x.key = x.getKey() - delta;
        }
        if (x.getKey() < this.min.getKey()) {
            this.min = x;
        }
        if (x.parent != null && x.getKey() < x.parent.getKey()) {
            cascadingCut(x, x.parent);
        }
        return;
    }


    /**
     * private void cascadingCut(HeapNode x, HeapNode y)
     *
     * cuts the node from its parent. Recursively: goes to the node's parent if its marked cuts it.
     * until we reach the root.   O(logn)
     *
     */

    private void cascadingCut(HeapNode x, HeapNode y){
       cut(x,y);
       cutCount++;
       this.numTrees++;
       if (y.parent != null) {  // if y is not a root
            if (!y.mark) {
                y.mark = true;
                this.notMarkedNodes-=1;
                return;
            }
            else {
                    cascadingCut(y, y.parent);

           }
        }

    }

    /**
     * private void cut(HeapNode x, HeapNode y)
     *
     * cuts the node from its parent. puts the node as new left tree in the heap  O(1)
     *
     */

    private void cut(HeapNode x, HeapNode y) {
        x.parent = null;
        if(x.mark){
            this.notMarkedNodes+=1;
        }
        x.mark = false;
        y.rank -= 1;
        if (x.next == x) {    // x has no sisters
            y.child = null;
        }
        else{
            x.prev.next = x.next;
            x.next.prev = x.prev;
            if (y.child == x){
                y.child = x.next;
            }
        }
        this.inNewLeftTree(x);
    }


   /**
    * public int nonMarked() 
    *
    * This function returns the current number of non-marked items in the heap O(1)
    */
    public int nonMarked() 
    {    
        return ( this.notMarkedNodes);
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap.    O(1)
    */
    public int potential() 
    {    
        return this.numTrees + ( 2 * (this.size()- this.nonMarked()));

    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.   O(1)
    */
    public static int totalLinks()
    {
        return FibonacciHeap.linkCount;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods).  O(1)
    */
    public static int totalCuts()
    {
        return FibonacciHeap.cutCount;
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
    * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
    *  
    * ###CRITICAL### : you are NOT allowed to change H.
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        if (H.isEmpty()){
            int[] arr = new int[0];
            return arr;
        }
        if (H.size < k) {
            k = H.size;
        }
        int[] array = new int[k];
         FibonacciHeap sortedHeap = new FibonacciHeap();
        int i = 0;
        HeapNode currentNode = H.findMin();
        array[i++] = currentNode.getKey();
        while (i < array.length) {
            if (currentNode.child != null) {
                HeapNode currentChild = currentNode.child;
                do {
                    sortedHeap.insert(currentChild.getKey());
                    sortedHeap.first.copy = currentChild;
                    currentChild = currentChild.next;
                }
                while (currentChild != currentNode.child);
            }
            array[i++] = sortedHeap.findMin().getKey();
            currentNode =  sortedHeap.findMin().copy;
            sortedHeap.deleteMin();
        }
        return array;
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in another file.  constructor cost O(1)
    *  
    */
    public static class HeapNode{

       public int key;
       private int rank;
       private boolean  mark;
       private HeapNode child;
       private HeapNode next;
       private HeapNode prev;
       private HeapNode parent;
       private HeapNode copy;

       public HeapNode(int key) {
           this.key = key;
           this.rank = 0;
           this.mark = false;
           this.child = null;
           this.next = this;
           this.prev = this;
           this.parent = null;
           this.copy = null;


    	}

       /**
        * getters for the feild - if needed O(1)
        *
        */
       public int getRank(){
           return this.rank;
       }
       public boolean getMarked(){
           return this.mark;
       }
       public HeapNode getNext(){
           return this.next;
       }
       public HeapNode getPrev(){
           return this.prev;
       }
       public HeapNode getChild(){
           return this.child;
       }
       public HeapNode getParent(){
           return this.parent;
       }


    	public int getKey() {
            return this.key;
    	}

       /**
        * public void AddAnotherChild(HeapNode child)
        *
        * used in linkHeaps function. helping function to delete min.
        * adds the bigger root to be the child of the smaller root. and coneccts their children with
        * the same rank.  O(1)
        */

       public void AddAnotherChild(HeapNode child)
       {
           if (this.rank >0)
           {
               child.next = this.child;
               child.prev = this.child.prev;
               this.child.prev.next = child;
               this.child.prev = child;
           }
           else{
               child.next = child;
               child.prev = child; 
           }
           this.child = child;
           child.parent = this;
           this.rank++;
       }
    }
}



