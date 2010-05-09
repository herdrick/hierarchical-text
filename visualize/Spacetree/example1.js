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

    //var json = {id: 'pairnode0.36679617549861065', name: '0.5505608733264988', data: {score: '0.5505608733264988'}, children: [{id: 'pairnode0.5702003671117492', name: '0.41202661849461425', data: {score: '0.41202661849461425'}, children: [{id: 'pairnode0.7761218513421033', name: '0.3508626860328792', data: {score: '0.3508626860328792'}, children: [{id: 'pairnode0.5481180008926875', name: '0.26009213571117396', data: {score: '0.26009213571117396'}, children: [{id: 'pairnode0.29990743125045505', name: '0.22319643681552884', data: {score: '0.22319643681552884'}, children: [{id: 'pairnode0.9514870084332815', name: '0.19200126455742422', data: {score: '0.19200126455742422'}, children: [{id: 'pairnode0.5208481693831897', name: '0.16678890325360068', data: {score: '0.16678890325360068'}, children: [{id: 'pairnode0.9384806268411826', name: '0.15141303075796425', data: {score: '0.15141303075796425'}, children: [{id: 'pairnode0.7036611173930658', name: '0.12930357273191254', data: {score: '0.12930357273191254'}, children: [{id: 'pairnode0.4906215723873192', name: '0.11877104233977248', data: {score: '0.11877104233977248'}, children: [{id: 'pairnode0.22609587833957046', name: '0.10211302032034605', data: {score: '0.10211302032034605'}, children: [{id: 'pairnode0.6012736641032961', name: '0.09254062955599512', data: {score: '0.09254062955599512'}, children: [{id: 'pairnode0.125653344786405', name: '0.09096172050362514', data: {score: '0.09096172050362514'}, children: [{id: 'leafnode0.04488448498589925', name: '/Chapter 6 of TLS has code problems.html', data: {}, children: [] } , {id: 'leafnode0.3171495324668834', name: '/Copy of index.html', data: {}, children: [] }] } , {id: 'leafnode0.666868698983156', name: '/Little schemer chapter 10.txt', data: {}, children: [] }] } , {id: 'leafnode0.7858225115773082', name: '/PCL vs. TLS.txt', data: {}, children: [] }] } , {id: 'leafnode0.7149115100599085', name: '/Lisp and Google.txt', data: {}, children: [] }] } , {id: 'pairnode0.13427787506175726', name: '0.08950545537727868', data: {score: '0.08950545537727868'}, children: [{id: 'leafnode0.8683557458567673', name: '/continuations.txt', data: {}, children: [] } , {id: 'leafnode0.9906455012184416', name: '/stray thoughts.txt', data: {}, children: [] }] }] } , {id: 'leafnode0.6149692641874451', name: '/thought on lisp and static typing.txt', data: {}, children: [] }] } , {id: 'leafnode0.1544503469908618', name: '/collectors continuations, and what you can do with them.txt', data: {}, children: [] }] } , {id: 'pairnode0.3536570335209247', name: '0.16575111185322336', data: {score: '0.16575111185322336'}, children: [{id: 'pairnode0.8535747908153011', name: '0.1435687194505838', data: {score: '0.1435687194505838'}, children: [{id: 'leafnode0.7755901693385262', name: '/emacs sucks.txt', data: {}, children: [] } , {id: 'leafnode0.951133669896502', name: '/Learning and ideas.txt', data: {}, children: [] }] } , {id: 'pairnode0.48090642424311836', name: '0.0', data: {score: '0.0'}, children: [{id: 'leafnode0.4190373072142639', name: '/loose thoughts copy.txt', data: {}, children: [] } , {id: 'leafnode0.537850465907369', name: '/loose thoughts.txt', data: {}, children: [] }] }] }] } , {id: 'pairnode0.3825554734745461', name: '0.14830520657443153', data: {score: '0.14830520657443153'}, children: [{id: 'leafnode0.4783510222660994', name: '/party archeology.html', data: {}, children: [] } , {id: 'leafnode0.7375106826180883', name: '/techcrunch party.html', data: {}, children: [] }] }] } , {id: 'pairnode0.5690823262044236', name: '0.20708244518464392', data: {score: '0.20708244518464392'}, children: [{id: 'pairnode0.9021420320861074', name: '0.17649624370469547', data: {score: '0.17649624370469547'}, children: [{id: 'leafnode0.6986636257795906', name: '/consitently decent.txt', data: {}, children: [] } , {id: 'leafnode0.045638121412371335', name: '/why hoop is now so much better to the eye.txt', data: {}, children: [] }] } , {id: 'leafnode0.8065694092407468', name: '/local eminence.txt', data: {}, children: [] }] }] } , {id: 'leafnode0.9444079801008632', name: '/littleschemer thoughts.txt', data: {}, children: [] }] } , {id: 'pairnode0.9524377091439677', name: '0.33421160680940654', data: {score: '0.33421160680940654'}, children: [{id: 'pairnode0.9680384416329891', name: '0.27561515420727967', data: {score: '0.27561515420727967'}, children: [{id: 'leafnode0.21878235519925704', name: '/evolution vs revolution paradox.txt', data: {}, children: [] } , {id: 'leafnode0.055395793352913336', name: '/On lisp.txt', data: {}, children: [] }] } , {id: 'leafnode0.06676966656460692', name: '/guitar.txt', data: {}, children: [] }] }] } , {id: 'leafnode0.5764130577051064', name: '/less is more in building software.txt', data: {}, children: [] }] };
  
    var json = {id: 'pairnode0.6194981379017408', name: 'you 0.0441, col 0.0331, only 0.0220, passed 0.0220', data: {score: '0.17698753561297983'} , children: [{id: 'pairnode0.158901013938341', name: 'choice 0.0159, taking 0.0159, public 0.0159, talk 0.0159', data: {score: '0.13849536649711813'} , children: [{id: 'leafnode0.4316195690191389', name: 'code 0.0064, aexp 0.0059, the -0.005, numbered 0.0048', data: {} , children: [] } , {id: 'leafnode0.5633527536078131', name: 'a 0.0234, software 0.0230, first 0.0230, is -0.018', data: {} , children: [] }] } , {id: 'leafnode0.4401730526776675', name: 'you 0.0441, the 0.0358, col 0.0331, is 0.0326', data: {} , children: [] }] }; 

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
