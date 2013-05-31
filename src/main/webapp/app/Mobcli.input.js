Ext.ns('Mobcli.input');

Ext.define('Mobcli.input.NodeModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {name: 'address', type: 'string'}, // full address
            {name: 'name', type: 'string'}, // name
            {name: 'value', type: 'string'}, // value
            {name: 'displayname', type: 'string'}, // display name
            {name: 'leaf', type: 'boolean'}, // is path or property, path is not leaf
            {name: 'generic', type: 'boolean', default: false} // is generic path
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
/*
            actionMethods: {
                read: 'POST'
            },
*/
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.nodes'
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
            emptyText: 'No nodes',
            listeners: {
                itemtap: function(list, index, target, record, e, eOpts) {
                    if(record.getData().leaf) {
                        Ext.Msg.alert("Alert", "Couldn't expand a property.");
                    }
                    else if(record.getData().generic) {
                        Ext.Msg.alert("Alert", "Couldn't expand a generic path.");
                    }
                    else {
                        Ext.getCmp('inputNavigationView').pushNodeListView(record.getData());
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
            {name: 'operation-name', type: 'string'}, // name
            {name: 'description', type: 'string'}, // description
            {name: 'request-properties'}, // prams
            {name: 'reply-properties'}, // prams
            {name: 'read-only', type: 'boolean'} // prams
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
/*
            actionMethods: {
                read: 'POST'
            },
*/
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            reader: {
                type: 'json',
                rootProperty: 'data.operations'
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
        address: null,
        ui: 'round',
        singleton: true,
        itemTpl: '<div>{operation-name} <small>({description}<small>)</div>',
        emptyText: 'No operation'
    },
    showOperationPanel: function(operation) {
        //TODO: memory leak???
        var popup = Ext.create('Mobcli.input.OperationPanel', {
            operation: operation,
            address: this.getAddress()
        });
        Ext.Viewport.add(popup);            
    }
});

Ext.define("Mobcli.input.OperationPanel", {
        extend: 'Ext.Panel',
        config: {
            address: null,
            operation: null,
            modal : true,
            centered : true,
            width : '90%',
            height : '80%',
            layout: 'fit',
            hideOnMaskTap: true,
            scrollable : true
        },
        
        initialize: function() {
            this.callParent();
            var operationPanel =  this;
            this.add(
                Ext.create('Ext.Toolbar', {
                    docked : 'top',
                    title: this.getAddress() + ':' + this.getOperation()['operation-name']
                })
            );
            
            var formPanel = Ext.create('Ext.form.Panel', {
                ui: 'round',
                standardSubmit : false,
                baseParams: {
                    operation: this.getOperation()
                },
                listeners: {
                    exception: function(){
                        Ext.Msg.alert('Exception', 'Failed to submit form');
                    },
                    beforesubmit: function(panel, values, options, eOpts ) {
                        console.log(values);                
                    }
                }
            });
            
            var fieldSet = Ext.create('Ext.form.FieldSet', {
                title: 'Parameters:',
                instructions: this.getOperation()['description']  + '<br>'
            });
            
            //add fields to FieldSet
            for(var paramName in this.getOperation()['request-properties']) {
                console.log(paramName);
                if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'STRING') {
                    fieldSet.add(Ext.create('Ext.field.Text',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        required: this.getOperation()['request-properties'][paramName]['required']
                    }));
                }
                else if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'BOOLEAN'){
                    fieldSet.add(Ext.create('Ext.field.Checkbox',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        value: 'true',
                        required: this.getOperation()['request-properties'][paramName]['required']
                    }));
                }
                else if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'INT'){
                    fieldSet.add(Ext.create('Ext.field.Number',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        minValue: this.getOperation()['request-properties'][paramName]['min'],
                        maxValue: this.getOperation()['request-properties'][paramName]['max'],
                        required: this.getOperation()['request-properties'][paramName]['required']
                    }));
                }
                else if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'BYTES'){
                    fieldSet.add(Ext.create('Ext.field.Spinner',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        minValue: this.getOperation()['request-properties'][paramName]['min'] ? this.getOperation()['request-properties'][paramName]['min'] : -127 ,
                        maxValue: this.getOperation()['request-properties'][paramName]['max'] ? this.getOperation()['request-properties'][paramName]['max'] : 127,
                        required: this.getOperation()['request-properties'][paramName]['required'],
                        stepValue: 1,
                        cycle: true
                    }));
                }
                else if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'LIST'){
                    //TODO: use textarea to input LIST, but needs good doc
                    fieldSet.add(Ext.create('Ext.field.TextArea',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        required: this.getOperation()['request-properties'][paramName]['required'],
                        placeHolder: this.getOperation()['request-properties'][paramName]['description']
                    }));

                }
                else if(this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE'] == 'OBJECT'){
                    //TODO: use textarea to input LIST, but needs good doc
                    fieldSet.add(Ext.create('Ext.field.TextArea',{
                        name: paramName,
                        label: paramName,
                        clearIcon: true,
                        required: this.getOperation()['request-properties'][paramName]['required'],
                        placeHolder: this.getOperation()['request-properties'][paramName]['description']
                    }));

                }
                else {
                    Ext.Msg.alert('Alert', 'Not supported parameter type: ' + this.getOperation()['request-properties'][paramName]['type']['TYPE_MODEL_VALUE']);
                }
                fieldSet.setInstructions(fieldSet.getInstructions() + "<br>" + "<b>" + paramName + ": </b>" +  this.getOperation()['request-properties'][paramName]['description']);
            }
            formPanel.add(fieldSet);
            var buttonContainer = Ext.create('Ext.Container', {
                layout: {type: 'hbox', pack: 'end'},
                defaults: {
                    xtype: 'button',
                    margin: 10
                },
                items: [
                    {
                        text: 'Submit',
                        handler: function(btn) {
                            var values = formPanel.getValues();
                            values.address = formPanel.getAddress();
                            values.operation = formPanel.getOperation();
                            Ext.Ajax.request({
                                url : 'cliservlet/execute',
                                params  : values,
//                                form: formPanel, // use form directly cause failure due to no enctype
                                method: 'GET',
                                success: function() {
                                    Ext.Msg.alert('Success', 'Form submitted successfully!');
                                },
                                failure: function() {
                                    Ext.Msg.alert('Failure', 'Failed to submit form!');
                                }
                            });
/*
//FIXME: !!! formPanel.submit doesn't submite form values, weird
                            formPanel.submit({
                                url: 'cliservlet/execute',
                                method: 'GET',
                                autoAbort: true,
                                waitMsg: {xtype: 'loadmask', message: 'Submitted, waiting...'},
                                params: {sleep: 2},
                                success: function() {
                                    Ext.Msg.alert('Success', 'Form submitted successfully!');
                                },
                                failure: function() {
                                    Ext.Msg.alert('Failure', 'Failed to submit form!');                                
                                }
                            });
*/

                        }
                    
                    },
                    {
                        text: 'Cancel',
                        handler: function(btn) {
                            operationPanel.hide();
                        }
                    }
                ]
            });
            formPanel.add(buttonContainer);
            this.add(formPanel);

        }
    }
);

Ext.define("Mobcli.input.NavigationView", {
    extend: "Ext.NavigationView",
    config: {
        items:[]
    },
    pushNodeListView: function(node) {
        var store = Ext.create("Mobcli.input.NodeStore"); 
        var newList = Ext.create('Mobcli.input.NodeListView',{
            address: node.address,
            store: store
        });
        this.push(newList);
        store.load({params: {node: Ext.JSON.encode(node)}});
    },
    pushOperationListView : function(node) {
        var operationListView = Ext.create('Mobcli.input.OperationListView', {
            address: node.address,
            listeners: {
                itemtap: function(list, index, target, record, e, eOpts) {
                    var leaf = record.getData().leaf;
//                Ext.Msg.alert("List Address", list.getAddress());

                    /*
                     Ext.create('Mobcli.input.FormPanel', {
                     name: record.getData()['operation-name'],
                     description: record.getData()['description']
                     }).show();
                     */
                    console.log("itemtap: " + record.getData()['description']);
                    list.showOperationPanel(record.getData());
                }
            }
        });
        if(!node.leaf) {
            operationListView.setTitle("OP:" + node.address);
        }
        else {
            // append displayname for attribute node
            operationListView.setTitle("OP:" + node.address + node.displayname);
        }
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
        store.load({params: {node: Ext.JSON.encode(node)}});        
    }
});

