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
    var json_good = {id: 'pairnode0.7075078898513146', name: 'you 0.0441, col 0.0331, only 0.0220, passed 0.0220', data: {score: '0.17698753561297983'} , children: [{id: 'pairnode0.27975738822314156', name: 'choice 0.0159, taking 0.0159, public 0.0159, talk 0.0159', data: {score: '0.13849536649711813'} , children: [{id: 'leafnode0.242448180221989', name: 'code 0.0064, aexp 0.0059, the -0.005, numbered 0.0048 /Chapter 6 of TLS has code problems.html', data: {} , children: [] } , {id: 'leafnode0.9833299129018764', name: 'a 0.0234, software 0.0230, first 0.0230, is -0.018 /consitently decent.txt', data: {} , children: [] }] } , {id: 'leafnode0.8695425133492058', name: 'you 0.0441, the 0.0358, col 0.0331, is 0.0326 /collectors continuations, and what you can do with them.txt', data: {} , children: [] }] };
  
    //begin json data
    var json ={id: 'pairing-node0.2012382698574111', name: 'ten 0.0407, thou 0.0347, thy 0.0297', data: {score: '0.2076191562046076'} , children: [{id: 'pairing-node0.8159633246707779', name: 'the 0.0348, thou 0.0268, my 0.0266', data: {score: '0.1798558483147529'} , children: [{id: 'pairing-node0.35650571977059975', name: 'the 0.0376, thy 0.0354, thou 0.0268', data: {score: '0.15401931738802932'} , children: [{id: 'pairing-node0.5220372660223997', name: 'thy 0.0431, thou 0.0351, of 0.0309', data: {score: '0.13626658053304233'} , children: [{id: 'document-node0.1385790584787976', name: 'thy 0.0549, and 0.0304, beauty 0.0304 : II', data: {} , children: [] } , {id: 'document-node0.32269938138068854', name: 'the 0.0480, thou 0.0480, of 0.0396 : III', data: {} , children: [] }] } , {id: 'pairing-node0.5703668208665997', name: 'the 0.0483, s 0.0314, world 0.0310', data: {score: '0.12473653057586384'} , children: [{id: 'document-node0.6073624029872275', name: 'the 0.0507, thy 0.0418, s 0.0330 : I', data: {} , children: [] } , {id: 'document-node0.8709498482623262', name: 'the 0.0460, in 0.0379, world 0.0379 : IX', data: {} , children: [] }] }] } , {id: 'pairing-node0.19748792896290135', name: 'my 0.0392, the 0.0320, that 0.0317', data: {score: '0.14038265724689417'} , children: [{id: 'document-node0.7896180686010971', name: 'that 0.0476, my 0.0392, the 0.0392 : L', data: {} , children: [] } , {id: 'document-node0.7086427923630018', name: 'with 0.0339, and 0.0339, the 0.0248 : V', data: {} , children: [] }] }] } , {id: 'pairing-node0.6872333431661427', name: 'thou 0.0427, ten 0.0407, thy 0.0388', data: {score: '0.13971712517268162'} , children: [{id: 'document-node0.5056553828860115', name: 'thy 0.0542, thou 0.0447, dost 0.0353 : IV', data: {} , children: [] } , {id: 'document-node0.9479123142948902', name: 'be 0.0407, thou 0.0407, ten 0.0407 : VI', data: {} , children: [] }] }] };//end json data
    
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
            height: 51,
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
            Log.write("showing");
        },
        
        onAfterCompute: function(){
            Log.write("done");
        },
        
        //This method is called on DOM label creation.
        //Use this method to add event handlers and styles to
        //your node.
	onCreateLabel: function(label, node){
	        label.id = node.id;            
	        label.innerHTML = node.name + (node.data.score ? (" " + (node.data.score + "").substring(0,4)) : "");
	        label.onclick = function(){
	        st.onClick(node.id);
        };
            //set label styles
            var style = label.style;
            style.width = 140 + 'px';
            style.height = 51 + 'px';            
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
		      node.data.$height = 51;
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
