;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 

; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance java.io.File o).  This is good Java interop juju.
(println "f")

(def DOC-COUNT 3)
(def DOC-OFFSET 0)
(def directory-string "/Users/herdrick/Dropbox/blog/to-classify")
(def all-txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) 
							       (into-array ["txt"]) true)))

(def txt-files (take DOC-COUNT (drop DOC-OFFSET all-txt-files)))

(defn ->vector [o]
  (if (vector? o) o [o]))

(defn file->seq [file]
  (re-seq #"[a-z]+" 
	  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))
 
(def omni-doc (apply concat (map file->seq txt-files)))
 
(defn freqs [words]
  (reduce (fn [freqs obj] 
	    (merge-with + freqs {obj 1})) 
	  {} words))

;takes a list of strings
(def words->relative-freq (memoize (fn [docu] 
				   (let [freqs (freqs docu)
					 word-count (count docu)]
				     (reduce (fn [rel-freqs key]
					       (conj rel-freqs [key (/ (float (freqs key)) word-count)])) ;would be clearer as (merge rel-freqs {key (/ (float (freqs key)) word-count)})   maybe
					     {} docu)))))


(def score first)
(def relfreqs second)
(def rfos-or-file  #(nth % 2))

(defn make-rfo [score relfreqs rfos-or-file]
  [score relfreqs rfos-or-file]) 

(defn make-rfo-minus-relfreqs [score rfos-or-file]
  (make-rfo score nil rfos-or-file))

;(def rfo-maybe? #(and (coll? %) (= 3 (count %))))

(def corpus-relfreq (words->relative-freq omni-doc)) 


(use '(incanter core stats)) ;need this only for abs and mean


;returns the euclidean distance between two documents
(defn euclid [relfreqs1 relfreqs2 omni-relfreq]
  (sqrt (reduce + (map (fn [[word freq]]
			 (sq (abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		       omni-relfreq))))


;todo: FALSE COMMENT here is where the huge number of relfreq distances are created (in rel-freq-distances) and immediately reduced to a single number (in score)

(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  


(def relative-freq (memoize (fn [rfo]
			      (println "rfo=" rfo)
			      (println)
			      (let [r-o-f (rfos-or-file rfo)]
			      (if (instance? java.io.File r-o-f)
				(words->relative-freq (file->seq r-o-f))
				(do ;(println "combining...")
				  (combine-relfreqs (relative-freq (first r-o-f)) 
						    (relative-freq (second r-o-f)))))))))
					;(instance? java.io.File rfos-or-file) (do ;(println "it's a file...")
					;					      (seq->relative-freq (file->seq rfos-or-file)))
			     ;(throw (new java.lang.Error (str "not an rfo! type: " (str (type rfos)) "    toString: " rfos))))))


(use 'clojure.contrib.combinatorics) ; sadly, combinations don't produce lazy seqs 

(def score-pair (fn [omni-relfreqs [rfo1 rfo2]] ; todo: make this a defn
		  (println "start s..p.")
		  (make-rfo-minus-relfreqs (euclid (relfreqs rfo1) (relfreqs rfo2) omni-relfreqs) 
					   [(make-rfo-minus-relfreqs (score rfo1) (rfos-or-file rfo1)) ;making a mock rfo here.  it lacks relfreqs but has the closure property (in the math/SICP sense) like all rfos
					    (make-rfo-minus-relfreqs (score rfo2) (rfos-or-file rfo2))]))) 

(defn best-pairing [rfos omni-relfreqs]
  (println "b.p.")
  (println "(count rfos)" (count rfos))
  (let [combos (sort (fn [rfo1 rfo2] (< (score rfo1) (score rfo2))) ; rfo1 and rfo2 are lacking relfreqs.  each represents a candidate pair - only the best scoring one will be made into a full rfo.
		     (map  (partial score-pair omni-relfreqs) 
			   (combinations 
			    rfos 
			    2)))
	best-pair (first combos)]
    (println "combos:")
    (println combos)
    (make-rfo (score best-pair) (relative-freq best-pair) (rfos-or-file best-pair))))


;(defn scored-pairings [rfos omni-relfreqs]
;  (sort (fn [rfo-pair-1 rfo-pair-2]
;	  (< (score-pair omni-relfreqs (first rfo-pair-1) (second rfo-pair-1)) 
;	     (score-pair omni-relfreqs (first rfo-pair-2) (second rfo-pair-2)))) 
;	(combinations rfos 2)))
 
(def docs-relfreqs (map #(make-rfo 99999999 (words->relative-freq (file->seq %)) %) txt-files))

;(println "last: " (last (combinations docs-relfreqs 2)))

;(defn filter-intersection [sequence sequences]
;  (filter (complement #(some (set sequence) %)) sequences))


;(defn fltr [rfos-pair rfos]
;  (filter (fn [rfo] 
;	    (some (set rfos-pair) 
	    
;(or (= (rfos-or-file rfo) (first (rfos-or-file rfos-pair))

     
(defn =rfos-ignore-relfreqs [rfo1 rfo2]
  (and (= (score rfo1) (score rfo2)) 
       (= (rfos-or-file rfo1) (rfos-or-file rfo2))))
	    

;this is the recursive thing that... pretty much is the master function. 
(defn foo [relfreqs omni-relfreq]
  (println "count relfreqs = " (count relfreqs))
  (println)
  (println)
  (println)
  (println "relfreqs:")
  (println relfreqs)
  (println)
  (println)
  (println)
  
  ;([relfreqs omni-relfreq] (foo (score-combos-n-sort relfreqs corpus-relfreq) relfreqs omni-relfreq))        
  (if (< (count relfreqs) 2) ; can't ever be 2, BTW.  3 choose 2 is 3, 2 choose 2 is 1. 
    relfreqs
    (let [best-pairing-rfo (best-pairing relfreqs omni-relfreq)
	  relfreqs-cleaned (filter (complement (fn [rfo]
						 (or (=rfos-ignore-relfreqs rfo (first (rfos-or-file best-pairing-rfo)))
						     (=rfos-ignore-relfreqs rfo (second (rfos-or-file best-pairing-rfo))))))
				   relfreqs)]
      (println "(rfos-or-file best-pairing-rfo) : " (rfos-or-file best-pairing-rfo))
      (println (type relfreqs-cleaned))

      (println (count relfreqs-cleaned))
      
      (println "relfreqs-cleaned = " relfreqs-cleaned)
     
      (foo (conj relfreqs-cleaned best-pairing-rfo) omni-relfreq))))

;here's how i'm calling this right now:
;(.replace (node (rest (first (foo docs-relfreqs corpus-relfreq)))) directory-string "")

;(filter (fn [rel

(defn fprn [s]
  (println s)
  s)


 