Ext.define('Mobcli.output.Panel', {
    extend: 'Ext.Panel',
    config: {
        layout: 'vbox',
        scrollable: 'both',
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Output'
            }
        ]
    },
    initialize: function() {
        this.callParent();
        // scroll to end automatically
        this.getScrollable().getScroller().on('maxpositionchange', function(scroller, max) {
            scroller.scrollTo(0, max.y);
        });
    },
    print: function(addressPath, operationResult) {
        var panel = Ext.create('Ext.Container',{
            items: [
                {
                    xtype: 'label',
                    padding: 5,
                    html: addressPath + "<br><pre>" + operationResult + "</pre> <hr>"
                }                       
            ]
        });
        this.add(panel);
    }
});