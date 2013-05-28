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
            url: 'cliservlet/resource',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.children'
            },

            listeners : {
                exception : function(proxy, response, operation) {
                    console.log(response);
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
            store: Ext.create("Mobcli.input.NodeStore"),
            itemTpl: '<div>{displayname}</div>',
            onItemDisclosure: true,
            listeners: {
                scope: this,
                itemtap: function(list, index, target, record, e, eOpts) {
                    var leaf = record.getData().leaf;
                    if(leaf) {
                        Ext.Msg.alert("Tap", "This is a property, couldn't expand.");
                    }
                    else {
                        var address = record.getData().address;
                        Ext.getCmp('inputNavigationView').pushNewList(address);
                    }

                },
                disclose: function(list, record, target, index, e, eOpts ) {
                    e.stopEvent();
                    //TODO: create command list view
                    var operationList = Ext.create('Mobcli.input.OperationListView');

                    Ext.getCmp('inputNavigationView').push(operationList);
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
        autoLoad: false
    }
});


Ext.define('Mobcli.input.OperationListView', {
    extend: 'Ext.List',
    config: {
        title: 'Operations',
        singleton: true,
        store: Ext.create('Mobcli.input.OperationStore'),
        itemTpl: '<div>{name}</div>'
    },
    initialize: function() {
        this.callParent();
//        this.getStore().load({params: {addr: this.getAddress()}});
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
        var newList = Ext.create('Mobcli.input.NodeListView',{address: address});
        this.push(newList);
        newList.getStore().load({params: {addr: newList.getAddress()}});
    }
});

