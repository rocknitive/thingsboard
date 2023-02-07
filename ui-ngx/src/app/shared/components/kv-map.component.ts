///
/// Copyright © 2016-2023 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, forwardRef, Input, OnInit } from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  Validator,
  Validators
} from '@angular/forms';
import { PageComponent } from '@shared/components/page.component';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'tb-key-val-map',
  templateUrl: './kv-map.component.html',
  styleUrls: ['./kv-map.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => KeyValMapComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => KeyValMapComponent),
      multi: true,
    }
  ]
})
export class KeyValMapComponent extends PageComponent implements ControlValueAccessor, OnInit, Validator {

  @Input() disabled: boolean;

  @Input() titleText: string;

  @Input() keyPlaceholderText: string;

  @Input() valuePlaceholderText: string;

  @Input() noDataText: string;

  kvListFormGroup: UntypedFormGroup;

  private destroy$ = new Subject();
  private propagateChange = null;

  constructor(protected store: Store<AppState>,
              private fb: UntypedFormBuilder) {
    super(store);
  }

  ngOnInit(): void {
    this.kvListFormGroup = this.fb.group({
      keyVals: this.fb.array([])
    });

    this.kvListFormGroup.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => this.updateModel());
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  keyValsFormArray(): UntypedFormArray {
    return this.kvListFormGroup.get('keyVals') as UntypedFormArray;
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (this.disabled) {
      this.kvListFormGroup.disable({emitEvent: false});
    } else {
      this.kvListFormGroup.enable({emitEvent: false});
    }
  }

  writeValue(keyValMap: {[key: string]: string}): void {
    const keyValsControls: Array<AbstractControl> = [];
    if (keyValMap) {
      for (const property of Object.keys(keyValMap)) {
        if (Object.prototype.hasOwnProperty.call(keyValMap, property)) {
          keyValsControls.push(this.fb.group({
            key: [property, [Validators.required]],
            value: [keyValMap[property], [Validators.required]]
          }));
        }
      }
    }
    this.kvListFormGroup.setControl('keyVals', this.fb.array(keyValsControls), {emitEvent: false});
    if (this.disabled) {
      this.kvListFormGroup.disable({emitEvent: false});
    } else {
      this.kvListFormGroup.enable({emitEvent: false});
    }
  }

  public removeKeyVal(index: number) {
    (this.kvListFormGroup.get('keyVals') as UntypedFormArray).removeAt(index);
  }

  public addKeyVal() {
    const keyValsFormArray = this.kvListFormGroup.get('keyVals') as UntypedFormArray;
    keyValsFormArray.push(this.fb.group({
      key: ['', [Validators.required]],
      value: ['', [Validators.required]]
    }));
  }

  public validate(c: UntypedFormControl) {
    const kvList: {key: string; value: string}[] = this.kvListFormGroup.get('keyVals').value;
    let valid = true;
    for (const entry of kvList) {
      if (!entry.key || !entry.value) {
        valid = false;
        break;
      }
    }
    return (valid) ? null : {
      keyVals: {
        valid: false,
      },
    };
  }

  private updateModel() {
    const kvList: {key: string; value: string}[] = this.kvListFormGroup.get('keyVals').value;
    const keyValMap: {[key: string]: string} = {};
    kvList.forEach((entry) => {
      keyValMap[entry.key] = entry.value;
    });
    this.propagateChange(keyValMap);
  }
}
