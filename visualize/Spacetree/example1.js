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

    var json = {id: 'pairnode0.687228978218739', name: 'summary here', data: {}, children: [{id: 'pairnode0.45345782194065365', name: 'summary here', data: {}, children: [{id: 'pairnode0.2690091324971857', name: 'summary here', data: {}, children: [{id: 'pairnode0.2875977053729414', name: 'summary here', data: {}, children: [{id: 'pairnode0.5256731144627205', name: 'summary here', data: {}, children: [{id: 'pairnode0.5334171925814875', name: 'summary here', data: {}, children: [{id: 'pairnode0.21064300521586965', name: 'summary here', data: {}, children: [{id: 'leafnode0.4449048821410575', name: '/Little schemer chapter 10.txt', data: {}, children: [] } , {id: 'leafnode0.9570263398434992', name: '/stray thoughts.txt', data: {}, children: [] }] } , {id: 'pairnode0.2548382925034699', name: 'summary here', data: {}, children: [{id: 'leafnode0.3497019178863007', name: '/PCL vs. TLS.txt', data: {}, children: [] } , {id: 'leafnode0.18782363663749857', name: '/posted this to reddit, mostly.txt', data: {}, children: [] }] }] } , {id: 'leafnode0.6275844446531337', name: '/Lisp and Google.txt', data: {}, children: [] }] } , {id: 'leafnode0.8684928908992396', name: '/thought on lisp and static typing.txt', data: {}, children: [] }] } , {id: 'pairnode0.19882354837252614', name: 'summary here', data: {}, children: [{id: 'leafnode0.6688219601498068', name: '/loose thoughts.txt', data: {}, children: [] } , {id: 'leafnode0.13211985512655466', name: '/loose thoughts2.txt', data: {}, children: [] }] }] } , {id: 'leafnode0.37592416174961396', name: '/On lisp.txt', data: {}, children: [] }] } , {id: 'leafnode0.43408807960291496', name: '/littleschemer thoughts.txt', data: {}, children: [] }] };

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
