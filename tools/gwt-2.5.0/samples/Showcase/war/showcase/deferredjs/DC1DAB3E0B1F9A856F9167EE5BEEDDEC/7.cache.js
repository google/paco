function iXb(){jXb.call(this,false)}
function GXb(a,b){IXb.call(this,a,false);this.b=b}
function HXb(a,b){IXb.call(this,a,false);FXb(this,b)}
function JXb(a){IXb.call(this,'GWT',true);FXb(this,a)}
function nlb(a){this.c=a;this.b=t5(this.c.a)}
function OWb(a,b){return VWb(a,b,a.a.b)}
function Pb(a,b){$b((ie(),de),a,tH(h0,Tmc,135,[(Cbc(),b?Bbc:Abc)]))}
function FXb(a,b){a.d=b;!!a.c&&hXb(a.c,a);if(b){b.cb.tabIndex=-1;yf();Pb(a.cb,true)}else{yf();Pb(a.cb,false)}}
function VWb(a,b,c){if(c<0||c>a.a.b){throw new tbc}a.o&&(b.cb[pvc]=2,undefined);NWb(a,c,b.cb);Thc(a.a,c,b);return b}
function p5(a){var b,c;b=DH(a.a.ld(Nvc),149);if(b==null){c=tH(m0,Umc,1,['New','Open',Ovc,Pvc,'Exit']);a.a.nd(Nvc,c);return c}else{return b}}
function o5(a){var b,c;b=DH(a.a.ld(Mvc),149);if(b==null){c=tH(m0,Umc,1,['Undo','Redo','Cut','Copy','Paste']);a.a.nd(Mvc,c);return c}else{return b}}
function s5(a){var b,c;b=DH(a.a.ld(Svc),149);if(b==null){c=tH(m0,Umc,1,['Contents','Fortune Cookie','About GWT']);a.a.nd(Svc,c);return c}else{return b}}
function r5(a){var b,c;b=DH(a.a.ld(Rvc),149);if(b==null){c=tH(m0,Umc,1,['Download','Examples',Zrc,"GWT wit' the program"]);a.a.nd(Rvc,c);return c}else{return b}}
function q5(a){var b,c;b=DH(a.a.ld(Qvc),149);if(b==null){c=tH(m0,Umc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.a.nd(Qvc,c);return c}else{return b}}
function MXb(){var a;Ni(this,$doc.createElement(utc));this.cb[Opc]='gwt-MenuItemSeparator';a=$doc.createElement(Tpc);DJb(this.cb,a);a[Opc]='menuSeparatorInner'}
function t5(a){var b,c;b=DH(a.a.ld(Tvc),149);if(b==null){c=tH(m0,Umc,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.a.nd(Tvc,c);return c}else{return b}}
function jlb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new nlb(a);n=new iXb;n.b=true;n.cb.style[Ppc]='500px';n.e=true;q=new jXb(true);p=q5(a.a);for(k=0;k<p.length;++k){MWb(q,new GXb(p[k],o))}d=new jXb(true);d.e=true;MWb(n,new HXb('File',d));e=p5(a.a);for(k=0;k<e.length;++k){if(k==3){OWb(d,new MXb);MWb(d,new HXb(e[3],q));OWb(d,new MXb)}else{MWb(d,new GXb(e[k],o))}}b=new jXb(true);MWb(n,new HXb('Edit',b));c=o5(a.a);for(k=0;k<c.length;++k){MWb(b,new GXb(c[k],o))}f=new jXb(true);MWb(n,new JXb(f));g=r5(a.a);for(k=0;k<g.length;++k){MWb(f,new GXb(g[k],o))}i=new jXb(true);OWb(n,new MXb);MWb(n,new HXb('Help',i));j=s5(a.a);for(k=0;k<j.length;++k){MWb(i,new GXb(j[k],o))}p4b(n.cb,npc,Uvc);gXb(n,Uvc);return n}
var Uvc='cwMenuBar',Mvc='cwMenuBarEditOptions',Nvc='cwMenuBarFileOptions',Qvc='cwMenuBarFileRecents',Rvc='cwMenuBarGWTOptions',Svc='cwMenuBarHelpOptions',Tvc='cwMenuBarPrompts';o1(661,1,{},nlb);_.nc=function olb(){sKb(this.b[this.a]);this.a=(this.a+1)%this.b.length};_.a=0;_.c=null;o1(662,1,Hnc);_.lc=function slb(){Y3(this.b,jlb(this.a))};o1(1059,102,Wmc,iXb);o1(1066,103,{100:1,105:1,119:1},GXb,HXb,JXb);o1(1067,103,{100:1,106:1,119:1},MXb);var xR=Xbc(nuc,'CwMenuBar$1',661),kX=Xbc(luc,'MenuItemSeparator',1067);uoc(wn)(7);