(ns useful.maps)

(defmacro assoc-if
  "Create mapping from keys to values in map if test returns true."
  [map test & kvs]
  `(if ~test
     (assoc ~map ~@kvs)
     ~map))

(defn assoc-or
  "Create mapping from each key to val in map only if existing val is nil."
  ([map key val]
     (if (nil? (map key))
       (assoc map key val)
       map))
  ([map key val & kvs]
     (let [map (assoc-or map key val)]
       (if kvs
         (recur map (first kvs) (second kvs) (nnext kvs))
         map))))

(defn into-map
  "Convert a list of heterogeneous args into a map. Args can be alternating keys and values,
   maps of keys to values or collections of alternating keys and values."
  [& args]
  (loop [args args map {}]
    (if (empty? args)
      map
      (let [arg  (first args)
            args (rest args)]
       (condp #(%1 %2) arg
         nil?  (recur args map)
         map?  (recur args (merge map arg))
         coll? (recur (into args (reverse arg)) map)
         (recur (rest args) (assoc map arg (first args))))))))

(defn map-vals
  "Create a new map from m by calling function f on each value to get a new value."
  [f m]
  (into {}
        (for [[k v] m]
          [k (f v)])))

(defn map-vals-with-keys
  "Create a new map from m by calling function f on each key and value to get a new value."
  [f m]
  (into {}
        (for [[k v] m]
          [k (f k v)])))

(defn update
  "Update value in map where f is a function that takes the old value and the supplied args and
   returns the new value. For efficiency, Do not change map if the old value is the same as the new
   value. If key is sequential, update all keys in the sequence with the same function."
  [map key f & args]
  (if (sequential? key)
    (reduce #(apply update %1 %2 f args) map key)
    (let [old (get map key)
          new (apply f old args)]
      (if (= old new) map (assoc map key new)))))

(defn merge-in
  "Merge two nested maps."
  [left right]
  (if (map? left)
    (merge-with merge-in left right)
    right))
