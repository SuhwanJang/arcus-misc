/* -*- Mode: Java; tab-width: 2; c-basic-offset: 2; indent-tabs-mode: nil -*- */
/*
 * acp-java : Arcus Java Client Performance benchmark program
 * Copyright 2016 JaM2in Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.collection.BTreeGetResult;
import net.spy.memcached.collection.ByteArrayBKey;
import net.spy.memcached.collection.CollectionAttributes;
import net.spy.memcached.collection.CollectionOverflowAction;
import net.spy.memcached.collection.CollectionResponse;
import net.spy.memcached.collection.Element;
import net.spy.memcached.collection.ElementFlagFilter;
import net.spy.memcached.collection.ElementFlagUpdate;
import net.spy.memcached.collection.ElementValueType;
import net.spy.memcached.collection.SMGetElement;
import net.spy.memcached.internal.CollectionFuture;
import net.spy.memcached.internal.CollectionGetBulkFuture;
import net.spy.memcached.internal.SMGetFuture;
import net.spy.memcached.ops.CollectionOperationStatus;

public class map_bulk_piped_ins implements client_profile {

  public boolean do_test(client cli) {
    try {
      if (!do_map_test(cli))
        return false;
    } catch (Exception e) {
      System.out.printf("client_profile exception. id=%d exception=%s\n",
          cli.id, e.toString());
      if (cli.conf.print_stack_trace)
        e.printStackTrace();
    }
    return true;
  }

  public boolean do_map_test(client cli) throws Exception {
    // Pick a key
    String key = cli.ks.get_key();

    String[] temp = key.split("-");
    long base = Long.parseLong(temp[1]);
    base = base * 64*1024;

    // Create a map item
    if (!cli.before_request())
      return false;
    ElementValueType vtype = ElementValueType.BYTEARRAY;
    CollectionAttributes attr =
        new CollectionAttributes(cli.conf.client_exptime,
            new Long(10000),
            CollectionOverflowAction.error);
    CollectionFuture<Boolean> fb = cli.next_ac.asyncMopCreate(key, vtype, attr);
    boolean ok = fb.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
    if (!ok) {
      System.out.printf("mop create failed. id=%d key=%s: %s\n", cli.id,
          key, fb.getOperationStatus().getResponse());
    }
    if (!cli.after_request(ok))
      return false;
    if (!ok)
      return true;

    // MopInsert Bulk (Piped)
    {
      Map<String, Object> elements = new HashMap<>();
      for (long mkey = base; mkey < base + 100; mkey++) {
        byte[] val = cli.vset.get_value();
        assert(val.length <= 4096);

        long n = mkey;
        int i = 0;
        while (n != 0 && i < val.length) {
          val[i] = (byte)(n % 10);
          n = n / 10;
          i++;
        }
        String input_mkey = Long.toString(mkey);
        elements.put(input_mkey, val);
      }
      if (!cli.before_request())
        return false;
      CollectionFuture<Map<Integer, CollectionOperationStatus>> f =
          cli.next_ac.asyncMopPipedInsertBulk(key, elements,
              new CollectionAttributes());
      Map<Integer, CollectionOperationStatus> status_map =
          f.get(cli.conf.client_timeout, TimeUnit.MILLISECONDS);
      Iterator<CollectionOperationStatus> status_iter =
          status_map.values().iterator();
      while (status_iter.hasNext()) {
        CollectionOperationStatus status = status_iter.next();
        CollectionResponse resp = status.getResponse();
        if (resp != CollectionResponse.STORED) {
          System.out.printf("Collection Map: MopPipedInsertBulk failed." +
                  " id=%d key=%s response=%s\n", cli.id,
              key, resp);
        }
      }
      if (!cli.after_request(true))
        return false;
    }

    return true;
  }
}
