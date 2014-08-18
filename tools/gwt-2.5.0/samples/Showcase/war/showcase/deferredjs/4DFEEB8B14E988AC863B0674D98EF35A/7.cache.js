function aDc(){bDc.call(this,false)}
function yDc(a,b){ADc.call(this,a,false);this.c=b}
function zDc(a,b){ADc.call(this,a,false);xDc(this,b)}
function BDc(a){ADc.call(this,'GWT',true);xDc(this,a)}
function i1b(a){this.d=a;this.c=oNb(this.d.b)}
function FCc(a,b){return NCc(a,b,a.b.c)}
function _b(a,b){kc((ue(),pe),a,Glb(hIb,R2c,135,[(yTc(),b?xTc:wTc)]))}
function NCc(a,b,c){if(c<0||c>a.b.c){throw new pTc}a.p&&(b.db[Xbd]=2,undefined);ECc(a,c,b.db);QZc(a.b,c,b);return b}
function xDc(a,b){a.e=b;!!a.d&&_Cc(a.d,a);if(b){(bAc(),b.db).tabIndex=-1;Kf();_b(a.db,true)}else{Kf();_b(a.db,false)}}
function kNb(a){var b,c;b=Qlb(a.b.qe(tcd),149);if(b==null){c=Glb(mIb,S2c,1,['New','Open',ucd,vcd,'Exit']);a.b.se(tcd,c);return c}else{return b}}
function jNb(a){var b,c;b=Qlb(a.b.qe(scd),149);if(b==null){c=Glb(mIb,S2c,1,['Undo','Redo','Cut','Copy','Paste']);a.b.se(scd,c);return c}else{return b}}
function nNb(a){var b,c;b=Qlb(a.b.qe(ycd),149);if(b==null){c=Glb(mIb,S2c,1,['Contents','Fortune Cookie','About GWT']);a.b.se(ycd,c);return c}else{return b}}
function mNb(a){var b,c;b=Qlb(a.b.qe(xcd),149);if(b==null){c=Glb(mIb,S2c,1,['Download','Examples',E8c,"GWT wit' the program"]);a.b.se(xcd,c);return c}else{return b}}
function lNb(a){var b,c;b=Qlb(a.b.qe(wcd),149);if(b==null){c=Glb(mIb,S2c,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.se(wcd,c);return c}else{return b}}
function EDc(){var a;Zi(this,$doc.createElement(aad));this.db[N5c]='gwt-MenuItemSeparator';a=$doc.createElement(S5c);tpc(this.db,a);a[N5c]='menuSeparatorInner'}
function oNb(a){var b,c;b=Qlb(a.b.qe(zcd),149);if(b==null){c=Glb(mIb,S2c,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.se(zcd,c);return c}else{return b}}
function e1b(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new i1b(a);n=new aDc;n.c=true;n.db.style[O5c]='500px';n.f=true;q=new bDc(true);p=lNb(a.b);for(k=0;k<p.length;++k){DCc(q,new yDc(p[k],o))}d=new bDc(true);d.f=true;DCc(n,new zDc('File',d));e=kNb(a.b);for(k=0;k<e.length;++k){if(k==3){FCc(d,new EDc);DCc(d,new zDc(e[3],q));FCc(d,new EDc)}else{DCc(d,new yDc(e[k],o))}}b=new bDc(true);DCc(n,new zDc('Edit',b));c=jNb(a.b);for(k=0;k<c.length;++k){DCc(b,new yDc(c[k],o))}f=new bDc(true);DCc(n,new BDc(f));g=mNb(a.b);for(k=0;k<g.length;++k){DCc(f,new yDc(g[k],o))}i=new bDc(true);FCc(n,new EDc);DCc(n,new zDc('Help',i));j=nNb(a.b);for(k=0;k<j.length;++k){DCc(i,new yDc(j[k],o))}dMc(n.db,m5c,Acd);$Cc(n,Acd);return n}
var Acd='cwMenuBar',scd='cwMenuBarEditOptions',tcd='cwMenuBarFileOptions',wcd='cwMenuBarFileRecents',xcd='cwMenuBarGWTOptions',ycd='cwMenuBarHelpOptions',zcd='cwMenuBarPrompts';oJb(750,1,{},i1b);_.sc=function j1b(){jqc(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;oJb(751,1,F3c);_.qc=function n1b(){TLb(this.c,e1b(this.b))};oJb(1145,104,U2c,aDc);oJb(1152,105,{100:1,105:1,119:1},yDc,zDc,BDc);oJb(1153,105,{100:1,106:1,119:1},EDc);var wxb=TTc(Vad,'CwMenuBar$1',750),iDb=TTc(Tad,'MenuItemSeparator',1153);s4c(Jn)(7);