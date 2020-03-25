package com.qx.test.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author guowenpeng
 * createTime 2020/3/22 1:07
 */
public class BTree {

    private int m;

    private BTreeNode root;

    public BTree(int m) {
        this.m = m;
    }

    public int getM() {
        return m;
    }

    public void setRoot(BTreeNode bTreeNode) {
        this.root = bTreeNode;
    }

    public int getMidLocation() {
        return m / 2;
    }

    public int getMinVNum() {
        return m / 2 - (m % 2 == 0 ? 1 : 0);
    }

    /**
     * 添加值
     * @param value
     */
    public void addValue(int value) {
        System.out.println("add value: " + value);
        if (root == null) {
            root = new BTreeNode(value);
            return;
        }

        BTreeFindResult bTreeFindResult = root.findValue(value);
        if (bTreeFindResult.findSuccess()) {
            System.out.println(value + " is Exists");
            return;
        }

        BTreeNode bTreeNode = bTreeFindResult.getFindResult();
        int location = bTreeFindResult.getLocation();
        bTreeNode.addNodeValue(value, location, this);
    }

    public boolean deleteValue(int value) {
        System.out.println("delete value: " + value);
        if (root == null) {
            return false;
        }

        BTreeFindResult bTreeFindResult = root.findValue(value);
        if (!bTreeFindResult.findSuccess()) {
            System.out.println(value + " is not Exists: deleteValue");
            return false;
        }

        BTreeNode bTreeNode = bTreeFindResult.getFindResult();
        bTreeNode.deleteNodeValue(value, bTreeFindResult.getLocation(), this);

        return true;
    }


    /**
     * 查找
     * @param value
     * @return
     */
    public BTreeNode find(int value) {
        if (root == null) {
            return null;
        }

        BTreeFindResult result = root.findValue(value);

        return result.getFindResult();
    }

    public void printBTree() {
        if (root == null || root.vList.isEmpty()) {
            System.out.println("print end");
        }

        List<Object> levelList = new ArrayList<>();
        levelList.add(root);
        int levelListSize = levelList.size();

        Object obj;
        BTreeNode p;
        int count = 0;
        List<Object> nextLevelList = new ArrayList<>();
        while (true) {
            if (count == levelListSize) {
                if (nextLevelList.isEmpty()) {
                    break;
                }

                count = 0;
                levelList = nextLevelList;
                levelListSize = levelList.size();
                nextLevelList = new ArrayList<>();
                System.out.println();
            }

            obj = levelList.get(count);
            if (obj == null) {
                System.out.print(" ");
                if (nextLevelList.size() > 0) {
                    nextLevelList.add(null);
                }
            } else {
                p = (BTreeNode) obj;
                boolean existsChild = p.pList != null && !p.pList.isEmpty();
                for (int i = 0; i < p.vList.size(); i++) {
                    System.out.print(p.vList.get(i));
                    if (i != p.vList.size() - 1) {
                        System.out.print("-");
                    }
                    if(existsChild){
                        nextLevelList.add(p.pList.get(i));
                        nextLevelList.add(null);
                        if (i == p.vList.size() - 1) {
                            nextLevelList.add(p.pList.get(p.vList.size()));
                            nextLevelList.add(null);
                        }
                    }
                }
            }

            count++;
        }

        System.out.println();
    }


    public static class BTreeNode{

        private BTreeNode parent;

        private List<Integer> vList;

        private List<BTreeNode> pList;

        public BTreeNode(int value) {
            vList = new ArrayList<>();
            vList.add(value);
        }

        public BTreeNode(int value, List<BTreeNode> pList) {
            vList = new ArrayList<>();
            vList.add(value);
            this.pList = pList;
        }

        public BTreeNode(List<Integer> oldVList, List<BTreeNode> oldPList, int pos,
                         int pPos, int len, BTreeNode parent) {
            vList = new ArrayList<>();
            vList.addAll(oldVList.subList(pos, len + pos));
            if (oldPList != null) {
                pList = new ArrayList<>();
                pList.addAll(oldPList.subList(pPos, pPos + len + 1));
                pList.forEach(item -> item.setParent(this));
            }

            this.parent = parent;
        }

        public void setParent(BTreeNode bTreeNode) {
            this.parent = bTreeNode;
        }

        public void setvList(List<Integer> vList) {
            this.vList = vList;
        }

        public void setpList(List<BTreeNode> pList) {
            this.pList = pList;
        }



        public BTreeFindResult findValue(int value) {

            for (int i = 0; i < vList.size(); i++) {
                if (vList.get(i) == value) {
                    return new BTreeFindResult(this, true, i);
                } else if (vList.get(i) > value) {
                    // 不需要继续向后查找，查找子节点即可
                    if (pList == null) {
                        return new BTreeFindResult(this, false, i);
                    }
                    return pList.get(i).findValue(value);
                } else {
                    // 要查找的值比vList[i]大
                    if(i == vList.size() - 1) {
                        // 已经查找到最后一个值，查找最大的孩子节点
                        if (pList == null) {
                            return new BTreeFindResult(this, false, i + 1);
                        }
                        return pList.get(i + 1).findValue(value);
                    }
                    // 向后查找
                }

            }

            return new BTreeFindResult(this, false, 0);
        }


        /**
         * 叶子节点添加值
         * @param value
         * @param location
         * @param bTree
         */
        public void addNodeValue(int value, int location, BTree bTree) {
            this.addValue(value, location);

            /**
             * 添加值后检查是否分裂
             */
            this.nodeSplit(bTree);
        }

        /**
         * @param value
         * @param bTree
         */
        public void deleteNodeValue(int value, int location, BTree bTree) {
            // 转换成删除其右孩子最左叶子节点的最小值或其左孩子最右节点的最大值(优先级：值个数)
            boolean rChild = true;
            BTreeNode p = null;
            if (pList != null) {
                // 右孩子
                p = pList.get(location + 1);
                if (pList.get(location).vList.size() > p.vList.size()) {
                    // 左孩子
                    rChild = false;
                    p = pList.get(location);
                }
            }

            while (p != null
                    && p.pList != null) {
                // 还有孩子节点
                int index = rChild ? 0 : p.pList.size() - 1;
                p = p.pList.get(index);
            }

            if (p != null) {
                int index = rChild ? 0 : p.vList.size() - 1;
                vList.set(location, p.vList.get(index));
                p.delValue(index);
            } else {
                // 没有右孩子，说明是叶子节点
                p = this;
                p.delValue(location);
            }

            p.balancedEndNode(bTree);

        }

        // 需要平衡：
        // 1.兄弟节点值个数大于minVNum：把父节点的值赋值给当前节点；把兄弟节点的值赋值给父节点,删除兄弟节点的值
        // 2.兄弟节点值个数不大于minVNum
        //   2.2 把父节点的值赋值给当前节点，删除父节点当前指针和父节点值
        //   2.3 把当前节点删除，当前节点的值赋值到兄弟节点
        //   2.4 如果父亲节点是根节点： 1.节点值为空，替换父节点；2.结束操作
        //   2.5 如果父亲节点值的个数是否>=minVNum,操作结束
        //   2.6 判断父亲节点能否进行合并，如果可以则进行合并，合并后继续调整父节点的父节点
        //   2.7 如果不能进行合并，进行左旋或右旋操作结束
        private void balancedEndNode(BTree bTree) {
            int minVNum = bTree.getMinVNum();
            if (vList.size() >= minVNum
                    || parent == null) {
                // 不需要平衡
                return;
            }

            // 找到节点在父节点的位置
            int pLocation = parent.findChildLocation(this);


            // 右兄弟
            boolean rChild = true;
            BTreeNode brother = null;
            if (pLocation == parent.pList.size() - 1) {
                rChild = false;
                brother = parent.pList.get(pLocation - 1);
            } else {
                brother = parent.pList.get(pLocation + 1);
            }
            BTreeNode lBrother = pLocation == 0 ? null : parent.pList.get(pLocation - 1);
            if (lBrother != null
                    && lBrother.vList.size() > brother.vList.size()) {
                rChild = false;
                brother = lBrother;
            }

            // 父节点值所在位置
            int vLocation = rChild ? pLocation : pLocation - 1;
            if (brother.vList.size() > bTree.getMinVNum()) {
                // 1.兄弟节点值个数大于minVNum

                // 把父节点的值赋值给当前节点
                this.addValue(parent.vList.get(vLocation), rChild ? vList.size() : 0);
                // 把兄弟节点的值赋值给父节点,删除兄弟节点的值
                int brotherVLocation = rChild ? 0 : brother.vList.size() - 1;
                parent.vList.set(vLocation, brother.vList.get(brotherVLocation));
                brother.delValue(brotherVLocation);
            } else {
                // 2兄弟节点值个数不大于minVNum

                // 2.1把父节点的值赋值给当前节点,删除父节点当前指针和父节点值
                this.addValue(parent.vList.get(vLocation), rChild ? vList.size() : 0);
                parent.delValue(vLocation);
                parent.delPValue(pLocation);
                // 2.2 当前节点删除，当前节点的值赋值到兄弟节点
                brother.addValues(vList, rChild);

                // 2.3 判断父亲节点的值是否>=minVNum
                if (parent == bTree.root ){
                    if (parent.vList == null
                            || parent.vList.isEmpty()) {
                        bTree.setRoot(brother);
                    }
                } else {
                    parent.balanced(bTree);
                }
            }
        }

        private void balanced(BTree bTree) {
            if (this == bTree.root
                    || vList.size() >= bTree.getMinVNum()) {
                return;
            }

            int pLocation = parent.findChildLocation(this);
            // 父节点定位的值大于孩子节点
//            boolean parentVMax = (pLocation == 0);
//            int vLocation = parentVMax ? pLocation : pLocation - 1;

            // 右兄弟
            boolean rChild = true;
            BTreeNode brother = null;
            if (pLocation == parent.pList.size() - 1) {
                rChild = false;
                brother = parent.pList.get(pLocation - 1);
            } else {
                brother = parent.pList.get(pLocation + 1);
            }
            BTreeNode lBrother = pLocation == 0 ? null : parent.pList.get(pLocation - 1);
            if (lBrother != null
                    && lBrother.vList.size() > brother.vList.size()) {
                rChild = false;
                brother = lBrother;
            }

            int vLocation = rChild ? pLocation : pLocation - 1;
            if (this.vList.size() + brother.vList.size() + 1 >= bTree.getM()) {
                // 不能进行合并,进行左旋或右旋(rChild：true左旋， false右旋)
                if (rChild) {
                    vList.add(parent.vList.get(vLocation));

                    pList.add(brother.pList.get(0));
                    brother.pList.get(0).setParent(this);
                    parent.vList.set(vLocation, brother.vList.get(0));
                    brother.delValue(0);
                    brother.delPValue(0);
                } else {
                    int bPLocation = brother.vList.size();
                    int bVLocation = bPLocation - 1;
                    vList.add(0, parent.vList.get(vLocation));

                    this.addPValue(0, true, brother.pList.get(bPLocation));
                    brother.pList.get(bPLocation).setParent(this);
                    parent.vList.set(vLocation, brother.vList.get(bVLocation));
                    brother.delValue(bVLocation);
                    brother.delPValue(bPLocation);
                }
            } else {
                // 可以进行合并


                // 父节点的值放到当前节点值列表中
                this.addValue(parent.vList.get(vLocation), rChild ? vList.size() : 0);
                // 删除父节点的值和指针(当前节点删除)
                parent.delValue(vLocation);
                parent.delPValue(pLocation);

                // 当前节点的值赋值到兄弟节点
                brother.addValues(vList, rChild);
                // 当前节点的孩子节点同样放到兄弟节点中
                brother.addPValues(pList, rChild);


                // 继续调整父节点
                parent.balanced(bTree);
            }
        }



        /**
         * 节点分裂
         * @param bTree
         */
        private void nodeSplit(BTree bTree) {
            if (vList.size() < bTree.getM()) {
                return;
            }

            // 节点分裂,添加
            int mid = bTree.getMidLocation();
            BTreeNode bTreeNode1 = new BTreeNode(vList, pList, 0, 0, mid, parent);
            BTreeNode bTreeNode2 = new BTreeNode(vList, pList, mid + 1, mid + 1,
                    bTree.getM() - mid - 1, parent);

            this.addParentValue(bTreeNode1, bTreeNode2, vList.get(mid), bTree);
        }

        /**
         * 节点分裂后，向父节点添加值
         * @param bTreeNode1
         * @param bTreeNode2
         * @param value
         * @param bTree
         */
        private void addParentValue(BTreeNode bTreeNode1, BTreeNode bTreeNode2, int value, BTree bTree) {
            if (parent == null) {
                parent = new BTreeNode(value, Arrays.asList(bTreeNode1, bTreeNode2));
                bTreeNode1.setParent(parent);
                bTreeNode2.setParent(parent);
                bTree.setRoot(parent);
                return;
            }

            int parentLocation = parent.findChildLocation(this);
            parent.addValue(value, parentLocation);
            parent.addPValue(parentLocation, false, bTreeNode1, bTreeNode2);

            parent.nodeSplit(bTree);
        }

        /**
         * 从头或者尾部添加数组
         * @param valueList
         * @param headFlag
         */
        private void addValues(List<Integer> valueList, boolean headFlag) {
            List<Integer> newVList = new ArrayList<>();
            if (headFlag) {
                newVList.addAll(valueList);
                newVList.addAll(vList);
            } else {
                newVList.addAll(vList);
                newVList.addAll(valueList);
            }

            vList = newVList;
        }

        private void addPValues(List<BTreeNode> bTreeNodeList, boolean headFlag) {
            if (bTreeNodeList == null) {
                System.out.println("error: addPValues");
                return;
            }
            bTreeNodeList.forEach(item -> item.setParent(this));

            List<BTreeNode> newPList = new ArrayList<>();
            if (headFlag) {
                newPList.addAll(bTreeNodeList);
                newPList.addAll(pList);
            } else {
                newPList.addAll(pList);
                newPList.addAll(bTreeNodeList);
            }


            pList = newPList;
        }

        /**
         * 添加值
         * @param value
         * @param location
         */
        private void addValue(int value, int location) {
            List<Integer> newVList = new ArrayList<>();
            if (location == 0) {
                newVList.add(value);
                newVList.addAll(vList);
            } else {
                newVList.addAll(vList.subList(0, location));
                newVList.add(value);
                if (location != vList.size()) {
                    newVList.addAll(vList.subList(location, vList.size()));
                }
            }


            this.vList = newVList;
        }

        /**
         * 添加孩子指针
         * @param location
         * @param insertFlag 更新/插入
         * @param bTreeNodes
         */
        private void addPValue(int location, boolean insertFlag, BTreeNode... bTreeNodes) {
            List<BTreeNode> newPList = new ArrayList<>();
            List<BTreeNode> addNodes = Arrays.asList(bTreeNodes);
            if (location == 0) {
                newPList.addAll(addNodes);
                newPList.addAll(insertFlag ? pList : pList.subList(1, pList.size()));
            }else{
                newPList.addAll(pList.subList(0, location));
                newPList.addAll(addNodes);
                if (location != pList.size()) {
                    newPList.addAll(pList.subList(insertFlag ? location : location + 1, pList.size()));
                }
            }

            this.pList = newPList;
        }

        /**
         * 删除节点中的v
         * @param location
         */
        private void delValue(int location) {
            vList.remove(location);
        }

        /**
         * 删除节点中的指针
         * @param location
         */
        private void delPValue(int location) {
            pList.remove(location);
        }

        private int findChildLocation(BTreeNode childNode) {
            for (int i = 0; i < pList.size(); i++) {
                if (childNode == pList.get(i)) {
                    return i;
                }
            }

            return pList.size() - 1;
        }

    }

    public static class BTreeFindResult{
        private BTreeNode bTreeNode;
        private boolean findFlag;
        private int location;

        public BTreeFindResult(BTreeNode bTreeNode, boolean findFlag, int location) {
            this.bTreeNode = bTreeNode;
            this.findFlag = findFlag;
            this.location = location;
        }

        public BTreeNode getFindResult() {
            return bTreeNode;
        }

        public int getLocation() {
            return location;
        }

        public boolean findSuccess() {
            return findFlag;
        }
    }
}
