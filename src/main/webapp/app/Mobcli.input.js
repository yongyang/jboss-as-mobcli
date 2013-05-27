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

Ext.define("Mobcli.input.NodeLoaderStore", {
    extend: 'Ext.data.Store',
    config: {
        model: 'Mobcli.input.NodeModel',
        autoLoad: false,
        address: "/",
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

Ext.define('Mobcli.NodeList', {
        extend: 'Ext.List',
        xtype: 'moblci_nodelist',
        config: {
            address: null, // default address /, should be set for different address
            title: null,
            grouper: function(record) {
                return record.get('ispath') ? "Property" : "Path";
            },
            masked: {
                xtype: 'loadmask',
                message: 'loading...'
            },
            itemTpl: '<div>{displayname}</div>',
            onItemDisclosure: true,
            listeners: {
                scope: this,
                itemtap: function() {
                    Ext.Msg.alert("Tap", "Tap");
                },
                disclose: function(el, record, target, index, e, eOpts ) {
                    e.stopEvent();
                    Ext.getCmp('inputNavigationView').push({
                        title: 'Commands',
                        html: 'Command List'
                    });
                    return false;
                }
            }
        },

        initialize: function() {
            this.callParent();
            this.setTitle('Path:' + this.getAddress());
            var store = Ext.create("Mobcli.input.NodeLoaderStore",{address: this.getAddress()});
            store.getProxy().setUrl(store.getProxy().getUrl() + "?" + Ext.Object.toQueryString({'addr': this.getAddress()}));
            store.getProxy().setExtraParams('addr', this.getAddress());
            this.setStore(store);
            console.log("initialize:" + this.getAddress());
            store.load();            
        }
    }
);