var Log = {
    elem: false,
    write: function(text){
        if (!this.elem) 
            this.elem = document.getElementById('log');
        this.elem.innerHTML = text;
        this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
    }
};

function addEvent(obj, type, fn) {
    if (obj.addEventListener) obj.addEventListener(type, fn, false);
    else obj.attachEvent('on' + type, fn);
};


function init(){
    function get(id) {
      return document.getElementById(id);  
    };



    //init data
    var json_good = {
        id: "node02",
        name: 'the, it, a',
        data: {"ethan" : "herdrick","newcolor" : "#444444"},
        children: [{
            id: "node13",
            name: "2.?",
            data: {},
            children: []}]};

    var json =  {id: 'pairnode0.8001788180513737', name: 'summary here', data: {}, children: [{id: 'pairnode0.36935284371509314', name: 'summary here', data: {}, children: [{id: 'pairnode0.8870840274175286', name: 'summary here', data: {}, children: [{id: 'pairnode0.008863679117423473', name: 'summary here', data: {}, children: [{id: 'pairnode0.4756051833569287', name: 'summary here', data: {}, children: [{id: 'leafnode0.7845339112898291', name: '/productivity/thoughts.txt', data: {}, children: [] } , {id: 'leafnode0.4980935182530414', name: '/stray thoughts.txt', data: {}, children: [] }] } , {id: 'pairnode0.8738331600902641', name: 'summary here', data: {}, children: [{id: 'leafnode0.42771965130640033', name: '/loose thoughts.txt', data: {}, children: [] } , {id: 'leafnode0.32444549864446026', name: '/wealth.txt', data: {}, children: [] }] }] } , {id: 'pairnode0.6600074134102791', name: 'summary here', data: {}, children: [{id: 'pairnode0.012400516281841467', name: 'summary here', data: {}, children: [{id: 'leafnode0.7736402746380077', name: '/macros and the dynamic HTML templates,, like JSP and ASP.txt', data: {}, children: [] } , {id: 'leafnode0.07930814894858429', name: '/seasoned schemer.txt', data: {}, children: [] }] } , {id: 'leafnode0.5718526584237925', name: '/scheme docs whine.txt', data: {}, children: [] }] }] } , {id: 'pairnode0.3480194821844542', name: 'summary here', data: {}, children: [{id: 'pairnode0.0980057412629961', name: 'summary here', data: {}, children: [{id: 'pairnode0.15801064197998216', name: 'summary here', data: {}, children: [{id: 'leafnode0.18136154059447207', name: '/thought on usability.txt', data: {}, children: [] } , {id: 'leafnode0.8985971594969178', name: '/washing machines.txt', data: {}, children: [] }] } , {id: 'pairnode0.1705601096000926', name: 'summary here', data: {}, children: [{id: 'leafnode0.9889925908463422', name: '/PCL vs. TLS.txt', data: {}, children: [] } , {id: 'leafnode0.47493874245338163', name: '/posted this to reddit, mostly.txt', data: {}, children: [] }] }] } , {id: 'pairnode0.47660630465941134', name: 'summary here', data: {}, children: [{id: 'pairnode0.1828082468072143', name: 'summary here', data: {}, children: [{id: 'leafnode0.9443437148897093', name: '/On lisp.txt', data: {}, children: [] } , {id: 'leafnode0.13943676520758364', name: '/steve yegge mind-body dualismt.txt', data: {}, children: [] }] } , {id: 'pairnode0.25765630856761346', name: 'summary here', data: {}, children: [{id: 'leafnode0.427234206272082', name: '/steve yegge made some people cry this week.txt', data: {}, children: [] } , {id: 'leafnode0.39444966859966923', name: '/why hoop is now so much better to the eye.txt', data: {}, children: [] }] }] }] }] } , {id: 'pairnode0.6310479168207264', name: 'summary here', data: {}, children: [{id: 'pairnode0.31557601159710813', name: 'summary here', data: {}, children: [{id: 'pairnode0.7302705366639063', name: 'summary here', data: {}, children: [{id: 'pairnode0.5085229857043189', name: 'summary here', data: {}, children: [{id: 'leafnode0.02744264815688957', name: '/productivity of getting it done vs. productivity of figuring out what it should be.txt', data: {}, children: [] } , {id: 'leafnode0.5081937982875049', name: '/some shit.txt', data: {}, children: [] }] } , {id: 'leafnode0.49415134738249167', name: '/tags and technorati problems.txt', data: {}, children: [] }] } , {id: 'pairnode0.7306145482992328', name: 'summary here', data: {}, children: [{id: 'leafnode0.03635311391307128', name: '/mini-projects (doesnt belong here, i know).txt', data: {}, children: [] } , {id: 'leafnode0.29038409980253155', name: '/problem in seasoned schemer, apparently.txt', data: {}, children: [] }] }] } , {id: 'pairnode0.9029776227059731', name: 'summary here', data: {}, children: [{id: 'pairnode0.8812100809309331', name: 'summary here', data: {}, children: [{id: 'pairnode0.1871223204646164', name: 'summary here', data: {}, children: [{id: 'leafnode0.12958122034208375', name: '/posts/boy girl probabilities.txt', data: {}, children: [] } , {id: 'leafnode0.7047684531306089', name: '/thought on lisp and static typing.txt', data: {}, children: [] }] } , {id: 'leafnode0.26127243257798893', name: '/some more shit.txt', data: {}, children: [] }] } , {id: 'pairnode0.5413082424702931', name: 'summary here', data: {}, children: [{id: 'leafnode0.20384505112745177', name: '/unix- ssh adventures.txt', data: {}, children: [] } , {id: 'leafnode0.9939672674725052', name: '/Wikipedia and Wookiepedia.txt', data: {}, children: [] }] }] }] }] };

    //end
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;
    //init canvas
    //Create a new canvas instance.
    var canvas = new Canvas('mycanvas', {
        'injectInto': 'infovis',
        'width': w,
        'height': h,
        'backgroundColor': '#1a1a1a'
    });
    //end
    
    //init st
    //Create a new ST instance
    var st = new ST(canvas, {
      levelsToShow: 4,
        //set duration for the animation
        duration: 800,
        //set animation transition type
        transition: Trans.Quart.easeInOut,
        //set distance between node and its children
        levelDistance: 50,
        //set node and edge styles
        //set overridable=true for styling individual
        //nodes or edges
        Node: {
            height: 20,
            width: 140,
            type: 'rectangle',
            color: '#aaa',
            overridable: true
        },
        
        Edge: {
            type: 'bezier',
            overridable: true
        },
        
        onBeforeCompute: function(node){
            Log.write("loading " + node.name);
        },
        
        onAfterCompute: function(){
            Log.write("done");
        },
        
        //This method is called on DOM label creation.
        //Use this method to add event handlers and styles to
        //your node.
	onCreateLabel: function(label, node){
            label.id = node.id;            
            label.innerHTML = node.name;
            label.onclick = function(){
                st.onClick(node.id);
            };
            //set label styles
            var style = label.style;
            style.width = 140 + 'px';
            style.height = 17 + 'px';            
            style.cursor = 'pointer';
            style.color = '#333';
            style.fontSize = '0.8em';
            style.textAlign= 'center';
            style.paddingTop = '3px';
        },
        
        //This method is called right before plotting
        //a node. It's useful for changing an individual node
        //style properties before plotting it.
        //The data properties prefixed with a dollar
        //sign will override the global node style properties.
        onBeforePlotNode: function(node){
            //add some color to the nodes in the path between the
            //root node and the selected node.
            if (node.selected) {
                node.data.$color = "#ff7";
            }
            else {
                delete node.data.$color;
                var GUtil = Graph.Util;
                //if the node belongs to the last plotted level
                //if(!GUtil.anySubnode(node, "exist")) {
                    //count children number
                    var count = 0;
                    GUtil.eachSubnode(node, function(n) { count++; });
                    //assign a node color based on
                    //how many children it has
		    if (count > 0) {  //branching node
		      node.data.$color =  '#daa';
		      
		    }
		    else { // leaf node
		      node.data.$color =  '#aaa';
		      node.data.$height = 34;
		    }
		    //node.data.$color = ['#aaa', '#daa', '#daa', '#daa', '#400', '#500'][count]; 
		    //node.data.$color = "#aaa";
	     //}
            }
        },


        

        //This method is called right before plotting
        //an edge. It's useful for changing an individual edge
        //style properties before plotting it.
        //Edge data proprties prefixed with a dollar sign will
        //override the Edge global style properties.
        onBeforePlotLine: function(adj){
            if (adj.nodeFrom.selected && adj.nodeTo.selected) {
                adj.data.$color = "#eed";
                adj.data.$lineWidth = 3;
            }
            else {
                delete adj.data.$color;
                delete adj.data.$lineWidth;
            }
        }
    });
    //load json data
    st.loadJSON(json);
    //compute node positions and layout
    st.compute();
    //optional: make a translation of the tree
    st.geom.translate(new Complex(-200, 0), "startPos");
    //emulate a click on the root node.
    st.onClick(st.root);
    //end
    //Add event handlers to switch spacetree orientation.
    var top = get('r-top'), 
    left = get('r-left'), 
    bottom = get('r-bottom'), 
    right = get('r-right');
    
    function changeHandler() {
        if(this.checked) {
            top.disabled = bottom.disabled = right.disabled = left.disabled = true;
            st.switchPosition(this.value, "animate", {
                onComplete: function(){
                    top.disabled = bottom.disabled = right.disabled = left.disabled = false;
                }
            });
        }
    };
    
    top.onchange = left.onchange = bottom.onchange = right.onchange = changeHandler;
    //end

}
