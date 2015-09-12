(ns datomodel.util
  (:require [clojure.pprint :as pp])
  (:require [clojure.repl]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Control Flow
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro returning
  "Compute a return value, then execute other forms for side effects.
  Like prog1 in common lisp, or a (do) that returns the first form."
  [value & forms]
  `(let [value# ~value]
     ~@forms
     value#))

(defmacro returning-bind
  "Compute a return value, bind that value to provided sym, then
  execute other forms for side effects within the lexical scope of
  that binding.  The return value of a returning-bind block will be
  the value computed by retn-form.  Similar in concept to Paul
  Graham's APROG1, or what is commonly found in CL libraries as
  PROG1-BIND.  This macro is especially handy when one needs to
  interact with stateful resources such as io.

  Example:

    (returning-bind [x (inc 41)]
      (println :returning x)
      (println 3.141592654))

  PRINTS:   :returning 42
            3.141592654
  RETURNS:  42"
  [[sym retn-form] & body]
      `(let [val# ~retn-form
             ~sym val#]
         ~@body
         val#))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.
  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (range) s))

(defn positions
  "Returns a lazy sequence containing the positions at which pred
   is true for items in coll."
  [pred coll]
  (for [[idx elt] (indexed coll) :when (pred elt)] idx))

(defn split-vec
  "Split the given vector at the provided offsets using subvec. Supports
  negative offsets."
  [v & ns]
  (let [ns (map #(if (neg? %) (+ % (count v)) %) ns)]
    (lazy-seq
     (if-let [n (first ns)]
       (cons (subvec v 0 n)
             (apply split-vec
                    (subvec v n)
                    (map #(- % n) (rest ns))))
       (list v)))))

(defn knit
  "Takes a list of functions (f1 f2 ... fn) and returns a new function F.
  F takes a collection of size n (x1 x2 ... xn) and returns a vector
      [(f1 x1) (f2 x2) ... (fn xn)]."
  [& fs]
  (fn [arg-coll] split-vec    (vec (map #(% %2) fs arg-coll))))

(defn rmerge
  "Recursive merge of the provided maps."
  [& maps]
  (if (every? map? maps)
    (apply merge-with rmerge maps)
    (last maps)))

(defn symbolic-name-from-var
  [var]
  (clojure.string/join "/" ((juxt (comp str :ns) :name) (meta var))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Debugging
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro wrap-fn [name args & body]
  `(let [old-fn# (var-get (var ~name))
         new-fn# (fn [& p#]
                   (let [~args p#]
                     (do ~@body)))
         wrapper# (fn [& params#]
                    (if (= ~(count args) (count params#))
                      (apply new-fn# params#)
                      (apply old-fn# params#)))]
     (alter-var-root (var ~name) (constantly wrapper#))))


(defmacro ppmx [form]
  `(do
     (pp/cl-format *out*  ";;; Macroexpansion:~%~% ~S~%~%;;; First Step~%~%"
       '~form)
     (pp/pprint (macroexpand-1 '~form))
     (pp/cl-format *out*  "~%;;; Full expansion:~%~%")
     (pp/pprint (macroexpand '~form))
     (println "")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IO
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn lines-of-file [file-name]
  (line-seq
    (java.io.BufferedReader.
      (java.io.InputStreamReader.
        (java.io.FileInputStream. file-name)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Exceptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defmacro exception [& [param & more :as params]]
  (if (class? param)
    `(throw (new ~param (str ~@(interpose " " more))))
    `(throw (Exception. (str ~@(interpose " " params))))))


(defmacro ignore-exceptions [& body]
  `(try
     ~@body
     (catch Exception e# nil)))





;; (defn to-byte-array [x]
;;   (let [baos (ByteArrayOutputStream.)
;;         oos (ObjectOutputStream. baos)]
;;     (pr oos x)
;;     (.close oos)
;;     (.toByteArray baos)))
