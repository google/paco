package com.google.android.apps.paco;

import com.pacoapp.paco.R;

class EulaLocaleHelper extends AndroidLocaleHelper<Integer> {

  @Override
  protected Integer getEnVersion() {
    return R.raw.eula;
  }

  @Override
  protected Integer getJaVersion() {
    return R.raw.eula_ja;
  }
  
  @Override
  protected Integer getFiVersion() {
    return R.raw.eula_fi;
  }    

  @Override
  protected Integer getPtVersion() {
    return R.raw.eula_pt;
  }    
}