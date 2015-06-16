package com.pacoapp.paco.utils;

import com.pacoapp.paco.R;

public class EulaLocaleHelper extends AndroidLocaleHelper<Integer> {

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