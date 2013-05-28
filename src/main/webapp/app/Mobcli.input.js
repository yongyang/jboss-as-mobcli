Ext.ns('Mobcli.input');

Ext.define('Mobcli.input.NodeModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {name: 'address', type: 'string'}, // full address
            {name: 'name', type: 'string'}, // name
            {name: 'value', type: 'string'}, // value
            {name: 'displayname', type: 'string'}, // display name
            {name: 'leaf', type: 'boolean'} // is path or property, path is not leaf
        ]
    }
});

Ext.define("Mobcli.input.NodeStore", {
    extend: 'Ext.data.Store',
    config: {
        model: 'Mobcli.input.NodeModel',
        autoLoad: false,
        data: [],
//        sorters: 'name',
/*
        grouper: function(record) {
            return record.get('leaf') ? "Property" : "Path";
        },
*/
        proxy: {
            type: 'ajax',
            url: 'cliservlet/resources',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.children'
            },

            listeners : {
                exception : function(proxy, response, operation) {
                    Ext.Msg.alert('ERROR', 'Exception caught');
                }
            }
        }
    }
});

Ext.define('Mobcli.input.NodeListView', {
        extend: 'Ext.List',
        config: {
            address: null, // default address /, should be set for different address
            title: null,
/*
            grouper: function(record) {
                return record.get('ispath') ? "Property" : "Path";
            },
*/
            singleton: false,
            itemTpl: '<div>{displayname}</div>',
            onItemDisclosure: true,
            emptyText: 'No items',
            listeners: {
                scope: this,
                itemtap: function(list, index, target, record, e, eOpts) {
                    var leaf = record.getData().leaf;
                    if(leaf) {
                        Ext.Msg.alert("Alert", "Couldn't expand a property.");
                    }
                    else {
                        var address = record.getData().address;
                        Ext.getCmp('inputNavigationView').pushNewList(address);
                    }

                },
                disclose: function(list, record, target, index, e, eOpts ) {
                    e.stopEvent();
                    Ext.getCmp('inputNavigationView').pushOperationListView(record.getData());
                    return false;
                }
            }
        },

        initialize: function() {
            this.callParent();
            this.setTitle('PATH:' + this.getAddress());
        }
    }
);


// Command List view
Ext.define('Mobcli.input.OperationModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {name: 'name', type: 'string'}, // name
            {name: 'description', type: 'string'}, // description
            {name: 'params', type: 'string'} // prams
        ]
    }

});

Ext.define('Mobcli.input.OperationStore', {
    extend: 'Ext.data.Store',
    config: {
        model: 'Mobcli.input.OperationModel',
        autoLoad: false,
        proxy: {
            type: 'ajax',
            url: 'cliservlet/operations',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.children'
            },

            listeners : {
                exception : function(proxy, response, operation) {
                    Ext.Msg.alert('ERROR', 'Exception caught');
                }
            }
            
        }
    }
});


Ext.define('Mobcli.input.OperationListView', {
    extend: 'Ext.List',
    config: {
        title: 'OP:',
        singleton: true,
        itemTpl: '<div>{name}</div>'
    },
    initialize: function() {
        this.callParent();
    }
});

Ext.define("Ext.input.NavigationView", {
    extend: "Ext.NavigationView",
    config: {
        id: 'inputNavigationView',
        title: 'Input',
        cls: 'card dark',
        iconCls: 'search',
        items:[]
    },
    pushNewList: function(address) {
        var store = Ext.create("Mobcli.input.NodeStore"); 
        var newList = Ext.create('Mobcli.input.NodeListView',{
            address: address,
            store: store
        });
        this.push(newList);
        store.load({params: {addr: newList.getAddress()}});
    },
    pushOperationListView : function(node) {
        var operationListView = Ext.create('Mobcli.input.OperationListView');
        operationListView.setTitle("OP:" + node.address);
        var store = operationListView.getStore();
        if(store != null) {
            store.setData({});
        }
        else {
            store = Ext.create('Mobcli.input.OperationStore');
            operationListView.setStore(store);
        }
        this.push(operationListView);
//        console.log(node);
        //TODO: don't use node, specify generic, leaf
        store.load({params: {node: Ext.JSON.encode(node)}});        
    }
});

