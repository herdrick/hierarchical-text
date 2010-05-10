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
    var json ={id: 'pairnode0.3423691233705385', name: 'you 0.0316, col 0.0214', data: {score: '0.16253561835386157'} , children: [{id: 'pairnode0.4466604513735758', name: 'choice 0.0164, taking 0.0164', data: {score: '0.13849536649711813'} , children: [{id: 'leafnode0.8037548541941457', name: 'code 0.0151, aexp 0.0139 /Chapter 6 of TLS has code problems.html', data: {} , children: [] } , {id: 'leafnode0.28347516506144177', name: 'software 0.0240, a 0.0236 /consitently decent.txt', data: {} , children: [] }] } , {id: 'pairnode0.5018487508131988', name: 'you 0.0316, the 0.0243', data: {score: '0.12108294761835955'} , children: [{id: 'leafnode0.03598992725714234', name: 'is 0.0363, you 0.0319 /collectors continuations, and what you can do with them.txt', data: {} , children: [] } , {id: 'leafnode0.31303967962176205', name: 'you 0.0313, the 0.0210 /continuations.txt', data: {} , children: [] }] }] };//end json data

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
