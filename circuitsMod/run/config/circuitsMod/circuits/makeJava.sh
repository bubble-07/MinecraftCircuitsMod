for dir in */
do
    dir=${dir%*/} 
    rm -f $dir/Implementation.class
    javac $dir/Implementation.java
done
