;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 
 
; somewhere in grep-able codespace i need to keep track of the idea that (file? o) is just (instance? java.io.File o).  This is good Java interop juju.

(def DOC-COUNT 99)
(def DOC-OFFSET 0)
(def directory-string "/Users/herdrick/Dropbox/blog/to-classify")
(def all-txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File directory-string) 
							       (into-array ["txt" "html"]) true)))

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

;(defn make-rfo [score relfreqs rfos-or-file]
;  [score relfreqs rfos-or-file]) 

(defn make-rfo [{:keys [score relfreqs rfos-or-file]}]
  [score relfreqs rfos-or-file]) 

(def corpus-relfreq (words->relative-freq omni-doc)) 

(use '(incanter core stats)) ;need this only for abs and mean

;returns the euclidean distance between two documents
(defn euclid [relfreqs1 relfreqs2 omni-relfreq]
  (sqrt (reduce + (map (fn [[word freq]]
			 (sq (abs (- (get relfreqs1 word 0) (get relfreqs2 word 0)))))
		       omni-relfreq))))



(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  


(def relative-freq (memoize (fn [rfo]
			      (let [r-o-f (rfos-or-file rfo)]
			      (if (instance? java.io.File r-o-f)
				(words->relative-freq (file->seq r-o-f))
				(do ;(println "combining...")
				  (combine-relfreqs (relative-freq (first r-o-f)) 
						    (relative-freq (second r-o-f)))))))))

(use 'clojure.contrib.combinatorics) ; sadly, combinations don't produce lazy seqs 

(def score-pair (fn [omni-relfreqs [rfo1 rfo2]] ; todo: make this a defn
		  (make-rfo {:score (euclid (relfreqs rfo1) (relfreqs rfo2) omni-relfreqs) 
			     :rfos-or-file [(make-rfo {:score (score rfo1) :rfos-or-file (rfos-or-file rfo1)}) ;making a mock rfo here.  it lacks relfreqs but has the closure property (in the math/SICP sense) like all rfos
					    (make-rfo {:score (score rfo2) :rfos-or-file (rfos-or-file rfo2)})]}))) 

(defn best-pairing [rfos omni-relfreqs]
  (let [combos (sort (fn [rfo1 rfo2] (compare (score rfo1) (score rfo2))) ; rfo1 and rfo2 are mock rfos, lacking relfreqs.  each represents a candidate pair - only the best scoring one will be made into a full rfo.
		     (map  (partial score-pair omni-relfreqs) 
			   (combinations rfos 2)))
	best-pair (first combos)]
    (make-rfo {:score (score best-pair) :relfreqs (relative-freq best-pair) :rfos-or-file (rfos-or-file best-pair)})))

(def docs-rfos (map #(make-rfo {:relfreqs (words->relative-freq (file->seq %)) :rfos-or-file %}) txt-files))
     
(defn =rfos-ignore-relfreqs [rfo1 rfo2]
  (and (= (score rfo1) (score rfo2)) 
       (= (rfos-or-file rfo1) (rfos-or-file rfo2))))
	    
(use 'clojure.walk)
;this is the recursive thing that... pretty much is the master function. 
(defn foo [rfos omni-relfreq]
  (if (< (count rfos) 2) ; can't ever be 2, BTW.  3 choose 2 is 3, 2 choose 2 is 1. 
    (postwalk-replace {(second (first rfos)) nil} rfos) ; this postwalk-replace (tree replace) is to axe the final matchup's relfreqs for readability
    (let [best-pairing-rfo (best-pairing rfos omni-relfreq)
	  rfos-cleaned (filter (complement (fn [rfo]
					     (or (=rfos-ignore-relfreqs rfo (first (rfos-or-file best-pairing-rfo)))
						 (=rfos-ignore-relfreqs rfo (second (rfos-or-file best-pairing-rfo))))))
			       rfos)]
      (foo (conj rfos-cleaned best-pairing-rfo) omni-relfreq))))

;here's how i'm calling this right now:
;(.replace (node (first (foo docs-rfos corpus-relfreq))) directory-string "")

(defn fprn [s]
  (println s)
  s)




 

(defn bar [{:keys [a b]}] (list a b))

;(bar {:a 1})
  
;(bar {:a 1 :b 2})
