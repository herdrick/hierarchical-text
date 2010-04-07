;Ideas for doc comparisions:
;counts hash
;counts hash and TWC  YES NO!
;porportions 
;summation of all inverses of the Euclidian distances
[["north-america" [:usa :miami]]] 



(defn member? [x sequ]
  (some #{x} sequ))

(defn print-coll [coll]
  (if (not (empty? coll))
    (do
      (println (first coll))
      (print-coll (rest coll)))))

;lol - yes this already exists in the core.  it's 'memoize'.  hilarious.
(let [c (ref {})]
  
  (defn cacher [f]
    (fn [& args]
      (let [c-val (c args)]
	(if c-val
	  (do
	    (println "from cache!")
	    c-val)
	  (let [val (apply f args)]
	    (dosync (ref-set c (conj (deref c) {args val})))
	    val)))))
  (defn get-cache []
    c))

;because error messages in clojure suck balls
(defn arg-printer [f]
  (fn [& args] 
    (apply println "args = " args)
    (apply f args)))

(def DOC-COUNT 16)
(def DOC-OFFSET 24)

(def all-txt-files (seq (org.apache.commons.io.FileUtils/listFiles (new java.io.File "/Users/herdrick/Dropbox") 
							       (into-array ["txt"]) true)))

(def txt-files (take DOC-COUNT (drop DOC-OFFSET all-txt-files)))

(defn file->seq [file]
  (re-seq #"[a-z]+" 
	  (org.apache.commons.lang.StringUtils/lowerCase (slurp (.toString file)))))

(def omni-doc (apply concat (map file->seq txt-files)))
 
(defn freqs [words]
  (reduce (fn [freqs obj] 
	    (merge-with + freqs {obj 1})) 
	  {} words))

;takes a list of strings
(def seq->relative-freq (memoize (fn [docu] 
				   ;(println (hash docu))
				   (let [freqs (freqs docu)
					 word-count (count docu)]
					;(print word-count)
				     (reduce (fn [rel-freqs key]
					       (conj rel-freqs [key (/ (float (freqs key)) word-count)])) ;would be clearer as (merge rel-freqs {key (/ (float (freqs key)) word-count)})   maybe
					     {} docu)))))
 
(defn val-subtract [key subtrahend m] 
  (- (m key) subtrahend))

(use '(incanter core stats)) ;need this only for abs and mean

;returns the signature of distances between two documents' word freqs
;returned Map has a count = to the count of omni-relfreq, i.e. it's huge
;can easily make this work for more than 2 relfreqs, i think by using apply on combine-dists
;refactor this using merge-into
(defn rel-freq-distances [relfreqs1 relfreqs2 omni-relfreq]
  (reduce (fn [acc [word relfreq]] 
  	    (conj acc [word (abs (/ (- (get relfreqs1 word 0) (get relfreqs2 word 0))
				    relfreq))])) 	  
	  {}  
	  omni-relfreq))
	   
;scores a single Map of relative frequency distances (each is the frequency distance (between two docs) of a single word). this has an entry for each word in the entire corpus, i.e. it's huge 
;this sucks, but kinda works.  good enough.
;put this in the scope of score-pair?
;uh, i think the / part is needless because all our rel-freq-distances have the same length (= to the length of the corpus word frequency map) and this is only for comparison.  todo: fix by killing it. UPDATE: somehow, when I did that it changed my tree result.  Have no clue why.  Try looking into that after I get the tree-display thing going, to some degree.
(defn score [rel-freq-distances]
  ;(println "(count rel-freq-distances)=" (count rel-freq-distances))
  (/ (reduce + (vals rel-freq-distances)) (count rel-freq-distances))) 

;here is where the huge number of relfreq distances are created (in rel-freq-distances) and immediately reduced to a single number (in score)
(defn score-pair [all-word-relfreqs [[relfreq1 file-or-cat1] [relfreq2 file-or-cat2]] ] ; todo: make this a defn
  [(score (rel-freq-distances relfreq1 relfreq2 all-word-relfreqs)) file-or-cat1 file-or-cat2])

(use 'clojure.contrib.combinatorics) ; sadly, combinations don't produce lazy seqs 

 
(defn score-combos-n-sort [relfreqs omni-relfreq]
  (let [rez (sort (fn [[n1 _ _] [n2 _ _]] (< n1 n2)) 
		  (map (partial score-pair omni-relfreq)
		       (combinations relfreqs 2)))]
    rez))



(defn combine-relfreqs [rf1 rf2]
  (merge-with #(mean [% %2]) rf1 rf2))  ; i'm just combining relfreqs taking their (unweighted) mean.  


(def relative-freq (memoize (fn [cat-or-file]
			      (if (and (coll? cat-or-file) (= (count cat-or-file) 2))
				(do (println "combining...")
				    (combine-relfreqs (relative-freq (first cat-or-file)) 
						      (relative-freq (second cat-or-file))))
				
				(if (= "class java.io.File" (str (type cat-or-file)))
				  (do (println "it's a file...")
				      (seq->relative-freq (file->seq cat-or-file)))
				  (new java.lang.Error (str "not file nor pair of freqs"))))))); this is a file instead of a collection of 

  
(def corpus-relfreq (seq->relative-freq omni-doc)) 

;make this pointfree
(def hundred-doc-relfreqs (map #(vector (seq->relative-freq (file->seq %)) %) txt-files))

;likely i shouldn't have made a general fn for this but instead just inlined it.  but doing it this way cleared my head - allowed me to think about this properly   
;DURRRRR this is filter-intersection: (filter #(some (set [2 3]) %) [[1 9] [3 4] [5  2 6]])
(defn filter-intersection [sequence sequences]
  (filter #(some (set sequence) %) sequences))

;this is the recursive thing that... pretty much is the master function. 
(defn foo
  ([relfreqs omni-relfreq] (foo (score-combos-n-sort relfreqs corpus-relfreq) relfreqs omni-relfreq))     
  ([s-s relfreqs omni-relfreq] 
     (if (< (count s-s) 2) ; can't ever be 2, BTW.  3 choose 2 is 3, 2 choose 2 is 1. 
       s-s
       (let [relfreqs-new (conj (filter-intersection (rest (first s-s)) relfreqs) [(relative-freq (rest (first s-s))) (rest (first s-s))])
	     s-s-new (score-combos-n-sort relfreqs-new omni-relfreq)]
	 (foo s-s-new relfreqs-new omni-relfreq)))))





  
;OK, I lost track of my original plan: compare the docs by their relative word frequency.  (that is, relative to themselves, but then compared to the relative word frequency of the entire corpus)

;STRATEGY:  use the big corpus from Norvig as my corpus, except I also must add whatever files I am going to compare, otherwise... blow.  
;OK here's what would be cool: using Incanter to plug in all the word frequencies (i.e. for each doc).  Then I can get the standard deveiation, which
; means I can get a probability density function which I can then use to make a kick ass function that shows how similar are the probabilities (in terms of 
; each word's global frequency)   
; However...!  First I'm going to use the fucked up version to see what it gives me.  After all, it's the recursive hierarchical classifier that I have made 
; which is perhaps new - or at least is unusual.  So do that first!
;
;OK, what I currently have uses all words in the corpus.  This should work well with the aforementioned Incanter pdf system for finding afinitity between the word frequencies among docs.  
; But it doesn't work well - I think - with my lesser techniques, so I think I should do something like: just use the union of words in both docs.  the presence of one unusual word frequency in one doc but a frequency of zero in the other would then be taken into account (which would not be the case if you used intersections of words like I did before).  But first, try it as is!!
;
;OK, so to figure out the 'score' of a doc combo with the current setup, I guess make sure all per-word affinities are positive and then average them.  Coolio.
;
;(side note: working with floats is much faster than with clojure Ratios. Better than order of mag improvement when I converted everything to floats before doing other stuff. 
;OK, then all I need is to concat those category relfreqs with the other ones, and I can just call the existing code.  Anything else, (like what i was boneheadedly doing, is opt!  premature!)
;need to functionize the s-s thing then.  this functionizing 

;Shit.  clojure.contrib/combinations calls count on its coll argument, so if thats a lazy seq, it gets forced.  that sucks for me here.
;Possibly I should write my own version that doesn't work that way?   Hmmmm..... very easy... i already did this.  BUT only useful if I don't want to look at the scores for all combos to see which are the closest!
;Crap.  I needed to NOT chain together operations to make this recursive classifier work.  My pair-em-up function is just useless, because a key feature of this classifier is to allow a doc to be paired with an existing category OR another doc.  To enable that, then when you pair up two elements, it's necessary to commpare the newly created classification against each other element, and add those scores to the mix.  How simplest to do it?

;WIWLN: finished filter-intersection, which is good.  I need to test it with small DOC-COUNTs. Then I need to write the code that uses it - i.e. that repeatedly gets the lowest score pair, saving the list of those somewhere, and then calls filter-intersection with its previous result, until there is only one pair left.
;OK, did that.

;need to make the thing that makes a relfreq hash for a category (i.e. from two elements)
;there's a voice in the back of my head saying that I should just stick with the balanced tree version for now.  nnot sure if that's easier/less code though...?  ah yes, having coded for about 10 min, i now recall: i am holding onto my old relfreqs now.  so i can use them to start over, or i can do the opt thing.
;AAAAHHHH!  Hmm.  OK, I've got to make some things be able to handle either a file or a category:  
;  * how to represent a category?  list of files?  new name?  i think this is fresh ground - i can do what i want here.  well, list of files makes sense.  but a tree structure implies it should be tree-ish, no?  So it could be a tree of it's constituant trees.  this is a good functional approach.  simple to build up.  you end up with a very easy to read structure - that's nice.  it doesn't do any caching of it's relfreqs.  that could be a good thing.  i'm going to recalc everything every time.  this means cleaner code.  Then i add caching afterwards.
;  * how to combine file and file, file and cat, and cat and cat? 
;      i'm thinking just go from scratch: reload each doc, and combine them or their relfreqs in some way to get a single relfreq (see below)
;  * OK, so to get a single relfreq from each tree (file or cat) do I just average their relfreqs (unweighted), or do I get all docs and string append them to make a (weighted) relfreq?  I like the former.  I think we give equal weight, no matter what the number of words behind each element.  Otherwise a single large doc somewhere will always dominate entire trees.  With equal weight you get the same effect with a single doc that mates up late in the tree, but in that case we can see what happened.  Also, it's simpler to code, *i think*.

; Advanced note: to combine trees (cats or files), use the probability density function that i get when i plug in all of the scores.  (obviously i gotta choose a distribution, ex. guassian).  Wait, this is meaningless.  With scores, I just want the smallest one.   

;OK: conj (first s-s) (or whatever) onto the end of the new pairs.   WIWLN: take this:  (get-relfreq (first s-s))     and conj it onto hundred-whatever and then get s-s again using that new hundo-blahblah

; next up:  it looks like my cacher and memoize are missing a lot of potential chache hits.  i'll ignore this for now.
; so... what to do?  I think turn it loose on some docs and check out the results.  
; maybe i should start including the scores in the tree of results?  i think so.  it's part of the discovery process.
; shit, somehow it appears that at least one file isn't getting cleaned out of the list of relfreqs following it being paired up.  Under these configs: (def DOC-COUNT 24) (def DOC-OFFSET 24), it happens with blog/Lisp and Google.txt .  That's weird.  Ignore for now.  Ah, it seems to always happen.  I think it's because the first pairup isn't filtered out.  Let's fix this after putting the scores into the trees, should be easier to see then.

; for blog post - this seems ot be a version of Tf-idf, except hte idf part.  i think mine is better.  

;need to make foo variadic to encompass bar.  was going to use fooob to make a fn variadic, but need to google when i have connectivity
;NEXT: put logging stuff in the lambda inside filter-intersection (to find doubling up bug)
; OK, for the refactor followup blog post, how about subclass the vector or maybe incanter vector thing so that it also holds a string.  then I can get the string back out even while using the matrix ops of incanter to do lots of stuff.  i really like this idea.
;WOW - deep observation: a memoized function is just like a hash (map), except that.  Or, you can say that memoizing a function eliminates the need for the map holding the resulting data.  Tentiative idea: any time I have a map, look at axing it and using a memoized fn
; consider rolling seq->relative-freq into relative-freq
;WIWLN: i think i figured out the trouble with the current bug.  I think i needed to filter-intersection the winner immediately before adding the winner, not two steps later.  
;      (I also figured out a *probably* superior way to deal with relfreqs - rely on the fact that they are memoized in the function, so just get them when needed, holding onto only the file-or-cat thing).  This needs some refactoring across at least three functions, tho, so better to do that after I try the above.  If the above does not fix the problem, then sure, do this to simplify the situation, verify it works in the same way as before (a true refactor) and only then get back to debugging!
;WIW WHEN LAUNDERING:  just need to log what i'm sending to score-pair as second arg. i think it's just a map.  that's not going to work.
; holy fucking crap - that was a pain in the ass.  back to where i was before with my plain old crappy bug.  
; DONE i think i can fix it by doing what i have in foo-hosed.  or I could refactor... no.  i was going to fix the bug, or at least try to by doing the foo-hosed thing.  now, if i could get online in this falafel joint i'd look up how to use git.  No, that was easy.
;now refactor 






;SCRAPHEAP
;good general purpose function.  note it returns two maps
;(defn map-intersection [map1 map2]
;  (let [intersected-keys (clojure.set/intersection (set (keys map1)) (set (keys map2)))] 
;    [(select-keys map1 intersected-keys) (select-keys map2 intersected-keys)]))

;probably useful, but not here
;(defn remove-multi [targets sequ] 
;  (remove #(member? % targets) sequ))

;i used this to explore some data i had produced.  basically it's for filter-intersection, but also allows me to do the inverse: filter out everything that *does* contain one of some sequence.  It could be handy for using map or other higher-level fns.
;(defn intersection-fn [sequence]
;  (fn [sequ]
;    (let [intersec (clojure.set/intersection (set sequ) 
;					     (set sequence))]
;      (if (empty? intersec)
;	false
;	intersec)))) 

;above is the older, shorter version that didn't use the general intersection-fn below. i think don't use this in the blog post cause it violates my principle of: don't make a new abstraction unless you're going to need it more than once.  I only use intersection-fn below once in the code so it's not worth it.  But actually I used it again in debugging so it really did pay off, maybe.  Hmm, maybe I shoulda just copied and pasted and made what i needed for that as a one-off thing.  Yeah, probably.
;BTW there should be a negation predicate maker for Clojure. is there? YES  'complement'
;(defn filter-intersection [sequence sequences]
  ;(prn "filter-intersection. sequence = " sequence)
;  (filter (fn [sequ]
	    ;(prn "sequ = " (rest sequ))
;	    (not ((intersection-fn sequence) sequ))) sequences))



;pair-em-up is obsolete.  it's for pairing all combos up in one pass, without recalculating.  Recalcing is neccesary if you want to allow a non-balanced tree of heiratchies. 
;(defn pair-em-up [pairs] ; pairs must be sorted
;  (if (empty? pairs) ; will need to also check for a count of only one, since I'm pairing things up.
;    []
;    (conj (pair-em-up (filter-intersection (rest (first pairs)) (rest pairs))) ;i'm getting rid of all pairs that have either of the first pair's components, and *then* recurring on that seq
;	  (first pairs)))) 

;(use 'clojure.set)
;(project [(first two-freqs) (second two-freqs)] (intersection (set (keys (first two-freqs))) (set (keys (second two-freqs)))))


;(defn scores-all-pairs [x xs all-word-relfreqs]
;  (map (fn [[[relfreq1 file1] [relfreq2 file2]]]
;	 (let [r-f-d (rel-freq-distances relfreq1 relfreq2 all-word-relfreqs)]
;	   [(score r-f-d) file1 file2])) ; note: rel-freq-distances returns a map of count equal to the count of all-word-relfreqs, i.e huge
;       (for [y xs] [x y])))

;(defn all-scores [relfreqs omni-relfreq]
;  (if (empty? relfreqs)
;    '()
;    (concat (scores-all-pairs (first relfreqs) (rest relfreqs) omni-relfreq) 
;	    (all-scores (rest relfreqs) omni-relfreq))))
  
;(defn interesting [rel-freq-distances]
;  (let [sorted (sort (fn [[k1 v1] [k2 v2]] (< v1 v2)) 
;		     rel-freq-distances)]
;    [(take 5 sorted) (take 5 (reverse sorted))]))

;cautionary tale.  this fn turns out to be just filtering over the (some set1 set2) idiom.  so i'm now doing that.  i have this code here to remember that mistake.
;(defn filter-intersection [sequence sequences]
;  (filter (fn [sequ] 
;	    (empty? (clojure.set/intersection (set sequ) 
;					      (set sequence)))) sequences))


;wtf is this? 
;(defn rescore [newlyweds old-pairs] (let [pairs (filter-intersection (rest newlyweds) old-pairs)]
;		   (concat pairs (map #(score-pair % omni-relfreq) pairs))))     ; notice that (map #(score-pair % omni-relfreq) shows up twice in the code, possible fn?





