for dir in */
do
    dir=${dir%*/} 
    rm -f $dir/Utils.class
    rm -f $dir/Utils.java
    rm -f $dir/Implementation.class
    cp $dir/../Utils.java $dir/Utils.java
    javac $dir/Utils.java $dir/Implementation.java 
done
